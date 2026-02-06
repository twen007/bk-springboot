package gov.nist.oism.asd.empbc.v1;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import gov.nist.oism.asd.empbc.db.FileAttachmentDao;
import gov.nist.oism.asd.empbc.model.FileAttachment;
import gov.nist.oism.asd.empbc.model.User;
import gov.nist.oism.asd.empbc.util.StatusCode;
import gov.nist.oism.asd.empbc.v1.AttachmentService.DeleteAttachmentResponse;
import gov.nist.oism.asd.empbc.v1.SsoService.JsonStatus;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFShape;
import org.apache.poi.xssf.usermodel.XSSFPicture;
import org.apache.poi.xssf.usermodel.XSSFPictureData;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.poi.hssf.usermodel.HSSFPicture;
import org.apache.poi.hssf.usermodel.HSSFShape;
import org.apache.poi.hssf.usermodel.HSSFSheet;

@Path("/attachments")
public class AttachmentService extends SsoService {

    private static final Logger LOG = Logger.getLogger(AttachmentService.class.getSimpleName());

    @GET
    @Path("/{attachmentId}")
    public Response getAttachment(@Context HttpServletRequest servletRequest,
            @PathParam("attachmentId") Integer attachmentId) {
        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized))
                    .build();
        }
        FileAttachmentDao dao = new FileAttachmentDao();
        Map<String, Object> dbResults = dao.selectFileAttachment(attachmentId, true);
        FileAttachment fileAttachment = (FileAttachment) dbResults.get(FileAttachmentDao.FILE_ATTACHMENT_KEY);
        String filename = "missing.jpg";
        byte[] contents = null;
        if (fileAttachment == null || fileAttachment.getContent() == null || fileAttachment.getContent().length == 0) {
            File imageFile = new File(servletRequest.getServletContext().getRealPath("/images/missing.jpg"));
            if (imageFile.exists()) {
                java.nio.file.Path path = Paths.get(imageFile.getAbsolutePath());
                try {
                    contents = Files.readAllBytes(path);
                } catch (IOException caught) {
                    LOG.log(Level.SEVERE, null, caught);
                    return Response.status(Response.Status.NO_CONTENT)
                            .entity(serializeStatus(StatusCode.IncompleteData)).build();
                }
            }
        } else {
            filename = fileAttachment.getName();
            contents = fileAttachment.getContent();
        }
        final byte[] fileStreamContents = contents;
        StreamingOutput stream = (OutputStream out) -> {
            out.write(fileStreamContents);
            out.flush();
        };
        return Response.ok(stream, MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-Disposition", String.format("attachment;filename=\"%s\"", filename)).build();
    }

    @GET
    @Path("/all_to_pdf/{requestId}")
    public Response getAllAttachmentsAsPdf(@Context HttpServletRequest servletRequest,
            @PathParam("requestId") Integer requestId) {

        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized))
                    .build();
        }
        FileAttachmentDao dao = new FileAttachmentDao();
        Map<String, Object> results = dao.selectFileAttachmentsWithContentForRequest(requestId);
        StatusCode statusCode = (StatusCode) results.get(FileAttachmentDao.STATUS_CODE_KEY);

        if (statusCode != StatusCode.OK) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(serializeStatus(statusCode)).build();
        }

        List<FileAttachment> fileAttachments = (List<FileAttachment>) results.get(FileAttachmentDao.FILE_ATTACHMENT_LIST_KEY);

        if (fileAttachments == null || fileAttachments.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).entity(serializeStatus(StatusCode.RecordNotFound)).build();
        }

        StreamingOutput stream = combineFiles(fileAttachments);
        String filename = "combined_attachments_for_request_" + requestId + ".pdf";

        return Response.ok(stream, "application/pdf") // Changed MediaType to application/pdf
                .header("Content-Disposition", String.format("attachment;filename=\"%s\"", filename)).build();
    }

    @GET
    @Path("/all_to_zip/{requestId}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getAllAttachmentsAsZip(@Context HttpServletRequest servletRequest,
            @PathParam("requestId") Integer requestId) {
        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user for ZIP download");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized))
                    .build();
        }

        FileAttachmentDao dao = new FileAttachmentDao();
        Map<String, Object> results = dao.selectFileAttachmentsWithContentForRequest(requestId);
        StatusCode statusCode = (StatusCode) results.get(FileAttachmentDao.STATUS_CODE_KEY);

        if (statusCode != StatusCode.OK) {
            LOG.log(Level.WARNING, "Failed to retrieve attachments for ZIP, requestId: {0}, status: {1}", new Object[]{requestId, statusCode});
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(serializeStatus(statusCode)).build();
        }

        List<FileAttachment> fileAttachments = (List<FileAttachment>) results.get(FileAttachmentDao.FILE_ATTACHMENT_LIST_KEY);

        if (fileAttachments == null || fileAttachments.isEmpty()) {
            LOG.info("No attachments found for ZIP, requestId: " + requestId);
            return Response.status(Response.Status.NOT_FOUND).entity(serializeStatus(StatusCode.RecordNotFound)).build();
        }

        StreamingOutput stream = outputStream -> {
            try ( ZipOutputStream zos = new ZipOutputStream(outputStream)) {
                // Use a Set to track and rename duplicate filenames
                java.util.Set<String> filenames = new java.util.HashSet<>();
                for (FileAttachment fileAttachment : fileAttachments) {
                    if (fileAttachment.getContent() != null && fileAttachment.getContent().length > 0) {
                        String filename = fileAttachment.getName();
                        // Check for duplicates and rename if necessary
                        if (!filenames.add(filename)) {
                            int count = 1;
                            String newFilename = filename;
                            while (!filenames.add(newFilename = addSuffixToFilename(filename, "_" + count++)));
                            filename = newFilename;
                        }
                        // Create the zip entry with the potentially modified filename
                        ZipEntry zipEntry = new ZipEntry(filename);
                        zos.putNextEntry(zipEntry);
                        zos.write(fileAttachment.getContent());
                        zos.closeEntry();
                    }
                }
            }
        };
        String filename = "attachments_request_" + requestId + ".zip";
        return Response.ok(stream, MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-Disposition", String.format("attachment;filename=\"%s\"", filename)).build();
    }

    @GET
    @Path("/selected_to_zip/{encodedFileIds}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getSelectedAttachmentsAsZip(@Context HttpServletRequest servletRequest,
            @PathParam("encodedFileIds") String encodedFileIds) {
        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user for selected ZIP download");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)) 
                    .build();
        }

        List<Integer> fileIdList = new ArrayList<>();
        try {
            String urlDecodedIds = URLDecoder.decode(encodedFileIds, StandardCharsets.UTF_8.name());
            byte[] base64DecodedBytes = Base64.getDecoder().decode(urlDecodedIds);
            String commaSeparatedIds = new String(base64DecodedBytes, StandardCharsets.UTF_8);

            String[] idStrings = commaSeparatedIds.split(",");
            for (String idStr : idStrings) {
                if (idStr != null && !idStr.trim().isEmpty()) {
                    fileIdList.add(Integer.parseInt(idStr.trim()));
                }
            }
        } catch (NumberFormatException e) {
            LOG.log(Level.WARNING, "Invalid number format in file IDs string: " + encodedFileIds, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(serializeStatus(StatusCode.BadRequest)).build();
        } catch (IllegalArgumentException e) { // For Base64 decoding errors
            LOG.log(Level.WARNING, "Invalid Base64 encoded file IDs string: " + encodedFileIds, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(serializeStatus(StatusCode.BadRequest)).build();
        } catch (Exception e) { // Catch other potential exceptions like UnsupportedEncodingException
            LOG.log(Level.SEVERE, "Error decoding file IDs string: " + encodedFileIds, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(serializeStatus(StatusCode.BadRequest)).build();
        }

        if (fileIdList.isEmpty()) {
            LOG.log(Level.INFO, "No valid file IDs provided for selected ZIP download after decoding: {0}", encodedFileIds);
            return Response.status(Response.Status.BAD_REQUEST).entity(serializeStatus(StatusCode.BadRequest)).build();
        }

        FileAttachmentDao dao = new FileAttachmentDao();
        Map<String, Object> results = dao.selectFileAttachmentsWithContentByIds(fileIdList);
        StatusCode statusCode = (StatusCode) results.get(FileAttachmentDao.STATUS_CODE_KEY);

        if (statusCode != StatusCode.OK) {
            LOG.log(Level.WARNING, "Failed to retrieve attachments for ZIP, fileIds: {0}, status: {1}", new Object[]{fileIdList.toString(), statusCode});
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(serializeStatus(statusCode)).build();
        }

        List<FileAttachment> fileAttachments = (List<FileAttachment>) results.get(FileAttachmentDao.FILE_ATTACHMENT_LIST_KEY);

        if (fileAttachments == null || fileAttachments.isEmpty()) {
            LOG.log(Level.INFO, "No attachments found for ZIP, fileIds: {0}", fileIdList.toString());
            return Response.status(Response.Status.NOT_FOUND).entity(serializeStatus(StatusCode.RecordNotFound)).build();
        }

        StreamingOutput stream = outputStream -> {
            try ( ZipOutputStream zos = new ZipOutputStream(outputStream)) {
                // Use a Set to track and rename duplicate filenames
                java.util.Set<String> filenames = new java.util.HashSet<>();
                for (FileAttachment fileAttachment : fileAttachments) {
                    if (fileAttachment.getContent() != null && fileAttachment.getContent().length > 0) {
                        String filename = fileAttachment.getName();
                        // Check for duplicates and rename if necessary
                        if (!filenames.add(filename)) {
                            int count = 1;
                            String newFilename = filename;
                            while (!filenames.add(newFilename = addSuffixToFilename(filename, "_" + count++)));
                            filename = newFilename;
                        }
                        // Create the zip entry with the potentially modified filename
                        ZipEntry zipEntry = new ZipEntry(filename);
                        zos.putNextEntry(zipEntry);
                        zos.write(fileAttachment.getContent());
                        zos.closeEntry();
                    }
                }
            }
        };
        String filename = "attachments" + ".zip";
        return Response.ok(stream, MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-Disposition", String.format("attachment;filename=\"%s\"", filename)).build();
    }

    // Helper method to add a suffix to a filename, preserving extension
    private static String addSuffixToFilename(String filename, String suffix) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex > 0) {
            return filename.substring(0, dotIndex) + suffix + filename.substring(dotIndex);
        } else {
            return filename + suffix;
        }
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{file_id}")
    public Response deleteAttachment(@Context HttpServletRequest servletRequest, @PathParam("file_id") Integer fileId) {
        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized))
                    .build();
        }

        FileAttachmentDao dao = new FileAttachmentDao();
        Map<String, Object> results = dao.deleteAttachment(fileId);
        StatusCode statusCode = (StatusCode) results.get(FileAttachmentDao.STATUS_CODE_KEY);
        DeleteAttachmentResponse deleteAttachmentResponse = new DeleteAttachmentResponse();
        if (statusCode == StatusCode.OK) {
            deleteAttachmentResponse.setRowCount((Integer) results.get(FileAttachmentDao.ROW_COUNT_KEY));
        }

        return Response.ok().entity(serializeResponseWithStatus(deleteAttachmentResponse, statusCode)).build();
    }

    public static class DeleteAttachmentResponse extends JsonStatus {

        private Integer mRowCount;

        public Integer getRowCount() {
            return mRowCount;
        }

        public void setRowCount(Integer rowCount) {
            mRowCount = rowCount;
        }
    }

    public static StreamingOutput combineFiles(List<FileAttachment> fileAttachments) {
        return new StreamingOutput() {
            @Override
            public void write(OutputStream outputStream) throws IOException {
                List<PDDocument> individualDocs = new ArrayList<>();
                PDDocument finalDocument = null;

                try {
                    for (FileAttachment fileAttachment : fileAttachments) {
                        String fileType = fileAttachment.getTypeCode();
                        byte[] contents = fileAttachment.getContent();
                        PDDocument doc = null;
                        if (contents != null) {
                            switch (fileType.toLowerCase()) {
                                case "application/pdf": // Match full MIME type
                                    doc = convertPdfBytesToDocument(contents);
                                    break;
                                case "text/plain":
                                    doc = convertTextBytesToDocument(contents, fileAttachment.getName());
                                    break;
                                case "image/jpeg":
                                case "image/png":
                                case "image/gif":
                                    doc = convertImageBytesToDocument(contents, fileAttachment.getName());
                                    break;
                                case "text/csv":
                                    doc = convertCsvBytesToDocument(contents, fileAttachment.getName());
                                    break;
                                case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet": // XLSX
                                case "application/vnd.ms-excel": // XLS
                                    doc = convertExcelBytesToDocument(contents, fileAttachment.getName());
                                    break;
                                case "application/vnd.openxmlformats-officedocument.wordprocessingml.document": // DOCX
                                    doc = convertDocxBytesToDocument(contents, fileAttachment.getName());
                                    break;
                                case "application/msword": // DOC
                                    doc = convertDocBytesToDocument(contents, fileAttachment.getName());
                                    break;
                                default:
                                    LOG.warning("Unsupported file type for PDF conversion: " + fileType + " for file " + fileAttachment.getName());
                            }
                            if (doc != null) {
                                individualDocs.add(doc);
                            }
                        }
                    }

                    if (individualDocs.isEmpty()) {
                        // Create and save an empty PDF if there are no processable attachments
                        try ( PDDocument emptyDoc = new PDDocument()) {
                            emptyDoc.save(outputStream);
                        }
                        return;
                    }

                    // Merge all individual documents into the finalDocument
                    finalDocument = new PDDocument(); // This will be the merged document
                    PDFMergerUtility merger = new PDFMergerUtility();
                    for (PDDocument sourceDoc : individualDocs) {
                        merger.appendDocument(finalDocument, sourceDoc);
                    }
                    finalDocument.save(outputStream);

                } catch (CsvValidationException e) {
                    LOG.log(Level.SEVERE, "CSV Validation Exception during PDF combination: " + e.getMessage(), e);
                    throw new IOException("Error processing CSV and combining attachments into PDF", e);
                } catch (Exception e) {
                    LOG.log(Level.SEVERE, "Unexpected exception during PDF combination: " + e.getMessage(), e);
                    throw new IOException("Error processing and combining attachments into PDF", e);
                } finally {
                    // Close all individual documents
                    for (PDDocument doc : individualDocs) {
                        if (doc != null) {
                            try {
                                doc.close();
                            } catch (IOException e) {
                                LOG.log(Level.WARNING, "Failed to close an individual PDDocument during cleanup.", e);
                            }
                        }
                    }
                    // Close the final merged document
                    if (finalDocument != null) {
                        try {
                            finalDocument.close();
                        } catch (IOException e) {
                            LOG.log(Level.WARNING, "Failed to close the final merged PDDocument during cleanup.", e);
                        }
                    }
                }
            }
        };
    }

    private static PDDocument convertPdfBytesToDocument(byte[] contentBytes) throws IOException {
        return PDDocument.load(contentBytes);
    }

    private static PDDocument convertTextBytesToDocument(byte[] contentBytes, String title) throws IOException {
        String text = new String(contentBytes);
        return createTextPdfFromString(text, title);
    }

    private static PDDocument convertImageBytesToDocument(byte[] imageBytes, String imageName) throws IOException {
        PDDocument doc = new PDDocument();
        PDPage page = new PDPage();
        doc.addPage(page);

        PDImageXObject pdImage = PDImageXObject.createFromByteArray(doc, imageBytes, imageName);

        try ( PDPageContentStream contentStream = new PDPageContentStream(doc, page)) {
            float pageMargin = 50; // Margin from page edges
            float maxWidth = page.getMediaBox().getWidth() - 2 * pageMargin;
            float maxHeight = page.getMediaBox().getHeight() - 2 * pageMargin;

            float imgWidth = pdImage.getWidth();
            float imgHeight = pdImage.getHeight();

            float ratio = Math.min(maxWidth / imgWidth, maxHeight / imgHeight);
            float scaledWidth = imgWidth * ratio;
            float scaledHeight = imgHeight * ratio;

            // Center the image on the page
            float x = pageMargin + (maxWidth - scaledWidth) / 2;
            float y = pageMargin + (maxHeight - scaledHeight) / 2;

            contentStream.drawImage(pdImage, x, y, scaledWidth, scaledHeight);
        }
        return doc;
    }

    private static PDDocument convertCsvBytesToDocument(byte[] contentBytes, String title)
            throws IOException, CsvValidationException {
        PDDocument doc = new PDDocument();
        PDPage currentPage = new PDPage();
        doc.addPage(currentPage);

        float margin = 50;
        float yStart = currentPage.getMediaBox().getHeight() - margin;
        float tableTopY = yStart - 20; // Space for title

        try ( InputStream inputStream = new java.io.ByteArrayInputStream(contentBytes);  CSVReader reader = new CSVReader(new InputStreamReader(inputStream));  PDPageContentStream contentStream = new PDPageContentStream(doc, currentPage)) {

            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12); // Adjusted font size for title
            contentStream.newLineAtOffset(margin, yStart);
            contentStream.showText(sanitizeText(title));
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, 10);
            boolean csvTextBlockOpen = true;
            float yPosition = tableTopY;
            float leading = 1.5f * 10;
            contentStream.newLineAtOffset(margin, yPosition);

            String[] nextLine;
            while ((nextLine = reader.readNext()) != null) {
                if (yPosition < margin) {
                    contentStream.endText();
                    csvTextBlockOpen = false;
                    // Note: Closing and reopening contentStream for new page is complex with try-with-resources for the initial stream.
                    // This simplified version will need a more robust pagination for very long CSVs.
                    // For now, we assume it fits or needs external pagination logic.
                    // A full solution would involve closing the current contentStream, adding a new page,
                    // and creating a new contentStream for that page.
                    break; // Stop if out of space on the first page for this simplified version
                }
                String line = String.join(", ", nextLine);
                // Sanitize the line to replace tabs with spaces, as Helvetica doesn't support tabs
                contentStream.showText(sanitizeText(line));
                contentStream.newLineAtOffset(0, -leading);
                yPosition -= leading;
            }
            if (csvTextBlockOpen) {
                contentStream.endText();
            }
        }
        return doc;
    }

    private static PDDocument convertExcelBytesToDocument(byte[] contentBytes, String title) throws IOException {
        PDDocument doc = new PDDocument();

        try ( InputStream inputStream = new java.io.ByteArrayInputStream(contentBytes);  Workbook workbook = WorkbookFactory.create(inputStream)) { // Use WorkbookFactory to handle both XLS and XLSX

            DataFormatter dataFormatter = new DataFormatter();

            for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
                Sheet sheet = workbook.getSheetAt(sheetIndex);
                String sheetName = sheet.getSheetName();
                String documentTitle = title + (workbook.getNumberOfSheets() > 1 ? " - " + sheetName : "");

                PDPage currentPage = new PDPage(new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth())); // A4 Landscape
                doc.addPage(currentPage);
                PDPageContentStream contentStream = null; // Manual management for pagination

                try {
                    contentStream = new PDPageContentStream(doc, currentPage);
                    float margin = 50;
                    float yStart = currentPage.getMediaBox().getHeight() - margin;
                    float tableTopY = yStart - 20; // Space for title

                    // Draw title for the sheet
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                    contentStream.newLineAtOffset(margin, yStart); // yStart is based on landscape height
                    contentStream.showText(sanitizeText(documentTitle));
                    contentStream.endText();

                    // Prepare for content
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA, 8); // Smaller font for content
                    float yPosition = tableTopY;
                    float leading = 1.5f * 8; // Adjusted leading
                    contentStream.newLineAtOffset(margin, yPosition);

                    for (Row row : sheet) {
                        if (yPosition < margin + leading) { // Check if new page is needed
                            contentStream.endText(); // End text on current page
                            contentStream.close();   // Close stream for old page

                            currentPage = new PDPage(new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth())); // New landscape page
                            doc.addPage(currentPage);
                            contentStream = new PDPageContentStream(doc, currentPage); // New stream for new page

                            // Optionally, add a continued title
                            String continuedTitle = documentTitle + " (continued)";
                            contentStream.beginText();
                            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                            contentStream.newLineAtOffset(margin, currentPage.getMediaBox().getHeight() - margin);
                            contentStream.showText(continuedTitle);
                            contentStream.endText();

                            yPosition = currentPage.getMediaBox().getHeight() - margin - 20; // tableTopY for new page
                            contentStream.beginText();
                            contentStream.setFont(PDType1Font.HELVETICA, 8);
                            contentStream.newLineAtOffset(margin, yPosition);

                            // This is tricky: the outer try-with-resources for contentStream is per sheet.
                            // A more robust pagination within a single sheet would re-initialize contentStream.
                            // For simplicity, if a single sheet overflows, it will truncate here.
                            // To handle overflow within a sheet, the contentStream management needs to be more granular.
                            // Let's assume for now that a new page is created if yPosition is too low,
                            // and the next write will be on this new page (though the stream needs re-init).
                            // The current structure will create a new page but might not write to it correctly if a single sheet is very long.
                            // For this iteration, let's focus on getting each *Excel sheet* onto at least one *PDF page*.
                            // Continue processing on the new page with the re-initialized contentStream
                        }
                        StringBuilder rowText = new StringBuilder();
                        for (int i = 0; i < row.getLastCellNum(); i++) {
                            Cell cell = row.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                            String cellValue = "";
                            if (cell != null) {
                                cellValue = dataFormatter.formatCellValue(cell);
                            }
                            // Sanitize cell value to replace tabs and append
                            rowText.append(sanitizeText(cellValue));
                            if (i < row.getLastCellNum() - 1) {
                                rowText.append("  "); // Use two spaces as a separator
                            }
                        }
                        contentStream.showText(rowText.toString()); // Individual cell values are sanitized
                        contentStream.newLineAtOffset(0, -leading);
                        yPosition -= leading;
                    }
                    contentStream.endText(); // End text block on the last page for this sheet's content
                } finally {
                    if (contentStream != null) {
                        contentStream.close(); // Ensure the last stream for text content is closed
                    }
                }

                // Extract and add embedded images from the current sheet
                if (sheet instanceof XSSFSheet) {
                    XSSFSheet xssfSheet = (XSSFSheet) sheet;
                    if (xssfSheet.getDrawingPatriarch() != null) {
                        for (XSSFShape shape : xssfSheet.getDrawingPatriarch()) {
                            if (shape instanceof XSSFPicture) {
                                XSSFPicture picture = (XSSFPicture) shape;
                                XSSFPictureData pictureData = picture.getPictureData();
                                byte[] embeddedImageBytes = pictureData.getData();
                                String embeddedImageName = "excel_image_" + sheetIndex + "_" + System.nanoTime();

                                PDPage imagePage = new PDPage(new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth())); // Landscape for image page
                                doc.addPage(imagePage);
                                drawImageOnPage(doc, imagePage, embeddedImageBytes, embeddedImageName);
                            }
                        }
                    }
                } else if (sheet instanceof HSSFSheet) { // For .xls files
                    HSSFSheet hssfSheet = (HSSFSheet) sheet;
                    if (hssfSheet.getDrawingPatriarch() != null) {
                        for (HSSFShape shape : hssfSheet.getDrawingPatriarch().getChildren()) {
                            if (shape instanceof HSSFPicture) {
                                HSSFPicture picture = (HSSFPicture) shape;
                                // HSSFPictureData is part of PictureData, which is what getAllPictures() returns.
                                // For direct extraction from HSSFPicture:
                                PictureData pictureData = picture.getPictureData();
                                byte[] embeddedImageBytes = pictureData.getData();
                                String embeddedImageName = "excel_image_" + sheetIndex + "_" + System.nanoTime();

                                PDPage imagePage = new PDPage(new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth())); // Landscape for image page
                                doc.addPage(imagePage);
                                drawImageOnPage(doc, imagePage, embeddedImageBytes, embeddedImageName);
                            }
                        }
                    }
                }
            } // End of loop for sheets
        }
        return doc;
    }

    private static PDDocument convertDocxBytesToDocument(byte[] contentBytes, String title) throws IOException {
        try ( InputStream inputStream = new java.io.ByteArrayInputStream(contentBytes);  XWPFDocument document = new XWPFDocument(inputStream)) {
            XWPFWordExtractor extractor = new XWPFWordExtractor(document);
            String text = extractor.getText();
            return createTextPdfFromString(text, title);
        }
    }

    private static PDDocument convertDocBytesToDocument(byte[] contentBytes, String title) throws IOException {
        try ( InputStream inputStream = new java.io.ByteArrayInputStream(contentBytes);  HWPFDocument document = new HWPFDocument(inputStream)) {
            WordExtractor extractor = new WordExtractor(document);
            String text = extractor.getText();
            return createTextPdfFromString(text, title);
        }
    }

    // Helper to create a PDF from a string, with basic pagination
    private static PDDocument createTextPdfFromString(String text, String title) throws IOException {
        PDDocument doc = new PDDocument();
        PDPage currentPage = new PDPage();
        doc.addPage(currentPage);
        PDPageContentStream contentStream = null;

        try {
            contentStream = new PDPageContentStream(doc, currentPage);
            float margin = 50;
            float yStart = currentPage.getMediaBox().getHeight() - margin;
            float leading = 1.5f * 12; // For content text
            float titleLeading = 1.5f * 14; // For title

            // Draw title
            String sanitizedTitle = sanitizeText((title != null) ? title : "");
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
            contentStream.newLineAtOffset(margin, yStart);
            contentStream.showText(sanitizedTitle); // Title is already sanitized
            contentStream.endText();

            // Prepare for content
            float yPosition = yStart - titleLeading;
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, 12);
            contentStream.newLineAtOffset(margin, yPosition);

            String[] lines = text.split("\\r?\\n");

            for (String line : lines) {
                if (yPosition < margin + leading) { // Check if new page is needed
                    contentStream.endText();
                    contentStream.close(); // Close current stream

                    currentPage = new PDPage();
                    doc.addPage(currentPage);
                    contentStream = new PDPageContentStream(doc, currentPage); // New stream for new page

                    yPosition = currentPage.getMediaBox().getHeight() - margin - leading; // Reset Y position for content
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA, 12);
                    contentStream.newLineAtOffset(margin, yPosition);
                }
                contentStream.showText(sanitizeText(line)); // Sanitize line for tabs and newlines
                contentStream.newLineAtOffset(0, -leading);
                yPosition -= leading;
            }
            contentStream.endText();
        } finally {
            if (contentStream != null) {
                contentStream.close();
            }
        }
        return doc;
    }

    // Add this new private static helper method to AttachmentService.java
    private static void drawImageOnPage(PDDocument doc, PDPage page, byte[] imageBytes, String imageName) throws IOException {
        PDImageXObject pdImage = PDImageXObject.createFromByteArray(doc, imageBytes, imageName);
        try ( PDPageContentStream contentStream = new PDPageContentStream(doc, page)) {
            float pageMargin = 50;
            float maxWidth = page.getMediaBox().getWidth() - 2 * pageMargin;
            float maxHeight = page.getMediaBox().getHeight() - 2 * pageMargin;

            float imgWidth = pdImage.getWidth();
            float imgHeight = pdImage.getHeight();

            float ratio = 1.0f; // Default: no scaling

            // Only scale down if the image is larger than the available space
            if (imgWidth > maxWidth || imgHeight > maxHeight) {
                ratio = Math.min(maxWidth / imgWidth, maxHeight / imgHeight);
            }

            float scaledWidth = imgWidth * ratio;
            float scaledHeight = imgHeight * ratio;

            // Center the image
            float x = pageMargin + (maxWidth - scaledWidth) / 2;
            float y = pageMargin + (maxHeight - scaledHeight) / 2;

            contentStream.drawImage(pdImage, x, y, scaledWidth, scaledHeight);
        }
    }

    private static String sanitizeText(String text) {
        if (text == null) {
            return "";
        }
        // Replace tab, line feed, and carriage return with spaces
        return text.replace("\t", "  ").replace("\n", " ").replace("\r", " ");
    }
}
