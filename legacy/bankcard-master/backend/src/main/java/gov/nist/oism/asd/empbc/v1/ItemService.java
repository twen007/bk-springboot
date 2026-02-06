package gov.nist.oism.asd.empbc.v1;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.nist.oism.asd.empbc.db.ItemDao;
import gov.nist.oism.asd.empbc.model.ChemicalItem;
import gov.nist.oism.asd.empbc.model.EaItem;
import gov.nist.oism.asd.empbc.model.FileAttachment;
import gov.nist.oism.asd.empbc.model.Item;
import gov.nist.oism.asd.empbc.model.ItemQueryParam;
import gov.nist.oism.asd.empbc.model.User;
import gov.nist.oism.asd.empbc.model.Lookup;
import gov.nist.oism.asd.empbc.model.PcItem;
import gov.nist.oism.asd.empbc.model.NistOrgData;
import gov.nist.oism.asd.empbc.util.ApiUtil;
import gov.nist.oism.asd.empbc.util.CommonUtil;
import gov.nist.oism.asd.empbc.util.NistOrgWSCalls;
import gov.nist.oism.asd.empbc.util.StatusCode;
import gov.nist.oism.asd.empbc.util.StringUtil;
import gov.nist.oism.asd.empbc.util.ValidatorUtil;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlElement;
import org.glassfish.jersey.media.multipart.BodyPartEntity;
import org.glassfish.jersey.media.multipart.ContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;

import gov.nist.oism.asd.empbc.v1.SsoService.JsonStatus;
import javax.ws.rs.BadRequestException;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Path("/items")
public class ItemService extends SsoService {

    private static final Logger LOG = Logger.getLogger(ItemService.class.getSimpleName());

    @GET
    @Path("/supportedDivision")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSupportedDivisions(@Context HttpServletRequest servletRequest) {

        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
        }

        List<NistOrgData> dataList = getSupportedDivisions(authenticatedUser.getPeopleId());

        GetSupportedDivisionsResponse resp = new GetSupportedDivisionsResponse();
        resp.setData(dataList);

        return Response.ok().entity(serializeResponseWithStatus(resp, StatusCode.OK)).build();
    }

    //TODO: should validate user is in role of (AO,BAO,BCH)
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getEAItemByCriteria(@Context HttpServletRequest servletRequest,
            @QueryParam("ouId") Integer ouId,
            @QueryParam("divCode") String divCode,
            @QueryParam("fromDate") String fromDateString,
            @QueryParam("toDate") String toDateString) {

        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
        }

        ItemQueryParam param = new ItemQueryParam();
        try {
            ValidatorUtil.parseDate(fromDateString, "fromDate");

            ValidatorUtil.parseDate(toDateString, "toDate");

            String divCodes = null;
            //division codes expect numbers and comma, so strip out anything else
            if (divCode != null) {
                divCodes = divCode.replaceAll("[^0-9 ,]", "");
            }

            param.setOuId(ouId);

            //add single quotes for division codes
            if (divCodes != null) {
                divCodes = "'" + divCodes.replace(",", "','") + "'";
            }
            param.setDivCode(divCodes);
            //param.setOrgCodes(orgCodes); //a comma separated list of org codes
            param.setFromDate(fromDateString);
            param.setToDate(toDateString);
        } catch (BadRequestException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(serializeStatus(StatusCode.BadRequest)).build();
        }

        ItemDao dao = new ItemDao();
        Map<String, Object> results = dao.selectEAItemsWithParams(param);
        StatusCode statusCode = (StatusCode) results.get(ItemDao.STATUS_CODE_KEY);
        GetEAItemByCriteriaResponse getEAItemByCriteriaResponse = new GetEAItemByCriteriaResponse();
        if (statusCode == StatusCode.OK) {
            List<EaItem> items = (List<EaItem>) results.get(ItemDao.ITEMS_LIST_KEY);
            getEAItemByCriteriaResponse.setData(items);
        }

        return Response.ok().entity(serializeResponseWithStatus(getEAItemByCriteriaResponse, statusCode)).build();
    }

    @GET
    @Path("/pcItems")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPcItemByCriteria(@Context HttpServletRequest servletRequest,
            @QueryParam("fy") Integer fy,
            @QueryParam("ouId") Integer ouId,
            @QueryParam("divisionId") Integer divId,
            @QueryParam("groupId") Integer grpId,
            @QueryParam("fromDate") String fromDateString,
            @QueryParam("toDate") String toDateString) {

        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
        }

        ItemDao.PcItemCriteria param = new ItemDao.PcItemCriteria();

        try {
            ValidatorUtil.parseDate(fromDateString, "fromDate");

            ValidatorUtil.parseDate(toDateString, "toDate");
        } catch (BadRequestException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(serializeStatus(StatusCode.BadRequest)).build();
        }

        param.setFy(fy);
        param.setOuId(ouId);
        param.setDivId(divId);
        param.setGrpId(grpId);
        param.setFromDate(fromDateString);
        param.setToDate(toDateString);

        ItemDao dao = new ItemDao();
        Map<String, Object> results = dao.selectPropertyCustodianItemsReport(param);
        StatusCode statusCode = (StatusCode) results.get(ItemDao.STATUS_CODE_KEY);
        GetPcItemByCriteriaResponse getPcItemByCriteriaResponse = new GetPcItemByCriteriaResponse();
        if (statusCode == StatusCode.OK) {
            List<PcItem> items = (List<PcItem>) results.get(ItemDao.ITEMS_LIST_KEY);
            getPcItemByCriteriaResponse.setData(items);
        }

        return Response.ok().entity(serializeResponseWithStatus(getPcItemByCriteriaResponse, statusCode)).build();
    }

    @GET
    @Path("/itemStatusType")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getItemStatusType(@Context HttpServletRequest servletRequest) {

        ItemDao dao = new ItemDao();
        Map<String, Object> results = dao.getItemStatusType();
        StatusCode statusCode = (StatusCode) results.get(ItemDao.STATUS_CODE_KEY);
        List<Lookup> lookups = new ArrayList<>();
        GetItemStatusTypeResponse getItemStatusTypeResponse = new GetItemStatusTypeResponse();
        if (statusCode == StatusCode.OK) {
            lookups = (List<Lookup>) results.get(ItemDao.ITEM_STATUS_TYPE_KEY);
            getItemStatusTypeResponse.setData(lookups);
        }

        return Response.ok().entity(serializeResponseWithStatus(getItemStatusTypeResponse, statusCode)).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{itemId}")
    public Response getItem(@Context HttpServletRequest servletRequest, @PathParam("itemId") Integer itemId) {
        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
        }

        ItemDao dao = new ItemDao();
        Map<String, Object> results = dao.selectItem(itemId);
        StatusCode statusCode = (StatusCode) results.get(ItemDao.STATUS_CODE_KEY);
        if (statusCode == StatusCode.OK) {
            Item item = (Item) results.get(ItemDao.ITEM_KEY);
            ChemicalItem chemicalItem = (ChemicalItem) results.get(ItemDao.CHEMICAL_ITEM_KEY);
            FileAttachment fileAttachment = (FileAttachment) results.get(ItemDao.SHOPPING_CART_KEY);
            if (fileAttachment != null) {
                return Response.ok().entity(serializeResponseWithStatus(populateGetShoppingCartItemResponse(item, fileAttachment), statusCode)).build();
            } else if (chemicalItem != null) {
                return Response.ok().entity(serializeResponseWithStatus(populateGetChemicalItemResponse(item, chemicalItem), statusCode)).build();
            } else {
                return Response.ok().entity(serializeResponseWithStatus(populateGetItemResponse(item), statusCode)).build();
            }
        }

        // Some kind of error has happened.
        return Response.ok().entity(serializeResponseWithStatus(new GetItemResponse(), statusCode)).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response postItem(@Context HttpServletRequest servletRequest, PutOrPostItemRequest putOrPostItemRequest) {
        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
        }

        Item item = setItemForPutOrPostItemRequest(putOrPostItemRequest);

        item.setShoppingCartFileId(putOrPostItemRequest.getFileId());
        item.setItemNotes(putOrPostItemRequest.getItemNotes());

        // By default, make the actuals the same as estimates.
        item.setActualPrice(putOrPostItemRequest.getUnitPrice());
        item.setActualQuantity(putOrPostItemRequest.getQuantity());

        ItemDao dao = new ItemDao();
        Map<String, Object> results;
        Boolean isChemical = putOrPostItemRequest.getIsChemical();
        if (isChemical != null && isChemical) {
            ChemicalItem chemicalItem = setChemicalItem(putOrPostItemRequest);
            results = dao.insertChemicaItem(item, chemicalItem, authenticatedUser.getPeopleId());
        } else {
            results = dao.insertItem(item, authenticatedUser.getPeopleId());
        }

        StatusCode statusCode = (StatusCode) results.get(ItemDao.STATUS_CODE_KEY);
        PostItemResponse postItemResponse = new PostItemResponse();
        if (statusCode == StatusCode.OK) {
            postItemResponse.setItemId((Integer) results.get(ItemDao.ID_KEY));
        }

        return Response.ok().entity(serializeResponseWithStatus(postItemResponse, statusCode)).build();
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{itemId}")
    public Response putItem(@Context HttpServletRequest servletRequest, PutOrPostItemRequest putOrPostItemRequest, @PathParam("itemId") Integer itemId) {
        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
        }

        Item item = setItemForPutOrPostItemRequest(putOrPostItemRequest);
        item.setId(itemId);

        //TODO: still doesn't handle the case when a user update an item from is chemical to not a chemical
        //in this case, the chem record in the item_chemical table should be deleted for the item
        ItemDao dao = new ItemDao();
        Map<String, Object> results;
        Boolean isChemical = putOrPostItemRequest.getIsChemical();
        if (isChemical != null && isChemical) {
            ChemicalItem chemicalItem = setChemicalItem(putOrPostItemRequest);
            results = dao.updateChemicaItem(item, chemicalItem);
        } else {
            results = dao.updateItem(item);
        }

        StatusCode statusCode = (StatusCode) results.get(ItemDao.STATUS_CODE_KEY);
        PutItemResponse putItemResponse = new PutItemResponse();

        return Response.ok().entity(serializeResponseWithStatus(putItemResponse, statusCode)).build();
    }

    private Item setItemForPutOrPostItemRequest(PutOrPostItemRequest putOrPostItemRequest) {
        Item item = new Item();

        item.setRequestId(putOrPostItemRequest.getRequestId());
        //item.setType("R");
        item.setVendorId(putOrPostItemRequest.getVendorId());
        item.setCatalogNumber(putOrPostItemRequest.getCatalogNumber());
        item.setItemName(putOrPostItemRequest.getItemName());
        item.setUnitIssue(putOrPostItemRequest.getUnitIssue());
        item.setDescription(putOrPostItemRequest.getDescription());
        item.setPrice(putOrPostItemRequest.getUnitPrice());
        item.setQuantity(putOrPostItemRequest.getQuantity());
        item.setPurpose(putOrPostItemRequest.getPurpose());
        item.setProjectTask(putOrPostItemRequest.getProjTask());
        item.setObjectClass(putOrPostItemRequest.getObjectClass());
        item.setIsShipping(putOrPostItemRequest.getIsShippingCost());
        //item.setItemNotes(putOrPostItemRequest.getItemNotes());
        //item.setDateReceived(putOrPostItemRequest.getDateReceived());
        Boolean isChemical = putOrPostItemRequest.getIsChemical();
        item.setChemical(isChemical);
        item.setIsTaggableEquipment(putOrPostItemRequest.isTaggableEquipment);

        return item;
    }

    @POST
    @Produces(MediaType.TEXT_PLAIN) // This is because IE gets an error if json is returned.
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response postShoppingCart(@Context HttpServletRequest servletRequest, FormDataMultiPart formParams) {
        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
        }

        Map<String, List<FormDataBodyPart>> formFields = formParams.getFields();
        Item item = new Item();
        item.setChemical(false); // Chemical must have a value for shopping cart, false by default.
        FileAttachment fileAttachment = new FileAttachment();
        for (String formKey : formFields.keySet()) {
            List<FormDataBodyPart> formParts = formFields.get(formKey);
            FormDataBodyPart bodyPart = formParts.get(0);
            switch (formKey) {
                case "requestId":
                    item.setRequestId(Integer.parseInt(bodyPart.getValue()));
                    fileAttachment.setRequestId(Integer.parseInt(bodyPart.getValue()));
                    break;

                case "vendorId":
                    item.setVendorId(Integer.parseInt(bodyPart.getValue()));
                    break;

                case "fileCategoryId":
                    fileAttachment.setCategoryId(Integer.parseInt(bodyPart.getValue()));
                    break;

                case "price":
                    item.setPrice(Double.parseDouble(bodyPart.getValue()));
                    break;

                case "isChemical":
                    item.setChemical(true);
                    break;

                case "file":
                    ContentDisposition contentDisposition = bodyPart.getContentDisposition();
                    String fileName = contentDisposition.getFileName();
                    fileAttachment.setName(fileName);
                    fileAttachment.setTypeCode(bodyPart.getMediaType().toString());
                    BodyPartEntity entity = (BodyPartEntity) bodyPart.getEntity();
                    BufferedInputStream in = new BufferedInputStream(entity.getInputStream());
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    try {
                        int bytesRead = 0;
                        while ((bytesRead = in.read()) != -1) {
                            out.write(bytesRead);
                        }
                        entity.close();

                        if (out.size() > 0) {
                            byte[] contents = out.toByteArray();
                            fileAttachment.setContent(contents);
                            fileAttachment.setSize(contents.length);
                        }
                        LOG.info(String.format("document: %s of type %s contains %d bytes.", fileAttachment.getName(), fileAttachment.getTypeCode(), out.size()));
                    } catch (IOException caught) {
                        LOG.log(Level.SEVERE, caught.getMessage(), caught);
                    }
                    break;
            }
        }

        ItemDao dao = new ItemDao();
        fileAttachment.setCreatedBy(authenticatedUser.getPeopleId());
        Map<String, Object> results = dao.insertShoppingCartItem(item, fileAttachment, authenticatedUser.getPeopleId());
        StatusCode statusCode = (StatusCode) results.get(ItemDao.STATUS_CODE_KEY);
        PostShoppingCartResponse postShoppingCartResponse = new PostShoppingCartResponse();
        if (statusCode == StatusCode.OK) {
            postShoppingCartResponse.setSuccess(true);
            postShoppingCartResponse.setFileId((Integer) results.get(ItemDao.SHOPPING_CART_FILE_ID_KEY));
            postShoppingCartResponse.setItemId((Integer) results.get(ItemDao.ID_KEY));
        } else {
            postShoppingCartResponse.setSuccess(false);
            postShoppingCartResponse.setCode(statusCode.getCode());
            postShoppingCartResponse.setDescription(statusCode.getDescription());
        }
        return Response.ok().entity(postShoppingCartResponse.toString()).build();
    }

    @POST
    @Produces(MediaType.TEXT_PLAIN) // This is because IE gets an error if json is returned.
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Path("/csv")
    public Response postCsvItem(@Context HttpServletRequest servletRequest, FormDataMultiPart formParams) {
//        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
//        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
//            LOG.info("Can't find SSO user");
//            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
//        }

        Map<String, List<FormDataBodyPart>> formFields = formParams.getFields();
        List<Item> items = null;
        boolean validFileType = false;
        for (String formKey : formFields.keySet()) {
            List<FormDataBodyPart> formParts = formFields.get(formKey);
            FormDataBodyPart bodyPart = formParts.get(0);
            switch (formKey) {

                case "file":
                    String[] allowedFileEndings = new String[]{".csv"};
                    ContentDisposition contentDisposition = bodyPart.getContentDisposition();
                    String fileName = contentDisposition.getFileName();
                    for (String fileEnding : allowedFileEndings) {
                        if (fileName.endsWith(fileEnding)) {
                            validFileType = true;
                            break;
                        }
                    }
                    BodyPartEntity entity = (BodyPartEntity) bodyPart.getEntity();
                    BufferedInputStream in = new BufferedInputStream(entity.getInputStream());
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    try {
                        int bytesRead = 0;
                        while ((bytesRead = in.read()) != -1) {
                            out.write(bytesRead);
                        }
                        entity.close();

                        if (out.size() > 0) {
                            String fileContents = new String(out.toByteArray());
                            items = Item.parseCsvFile(fileContents);
                        }
                    } catch (IOException caught) {
                        LOG.log(Level.SEVERE, caught.getMessage(), caught);
                    }
                    break;
            }
        }

//        ItemDao dao = new ItemDao();
//        Map<String, Object> results = dao.insertItem(item, authenticatedUser.getPeopleId());
//        StatusCode statusCode = (StatusCode) results.get(ItemDao.STATUS_CODE_KEY);
        PostShoppingCartResponse postShoppingCartResponse = new PostShoppingCartResponse();
//        if (statusCode == StatusCode.OK) {
//            postShoppingCartResponse.setSuccess(true);
//            postShoppingCartResponse.setFileId((Integer) results.get(ItemDao.SHOPPING_CART_FILE_ID_KEY));
//            postShoppingCartResponse.setItemId((Integer) results.get(ItemDao.ID_KEY));
//        }
//        else {
//            postShoppingCartResponse.setSuccess(false);
//            postShoppingCartResponse.setCode(statusCode.getCode());
//            postShoppingCartResponse.setDescription(statusCode.getDescription());
//        }
        return Response.ok().entity(postShoppingCartResponse.toString()).build();
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{itemId}/projectTask/{projectTask}")
    public Response putProjectTaskForRequestItems(@Context HttpServletRequest servletRequest, @PathParam("itemId") Integer itemId, @PathParam("projectTask") String projectTask) {
        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
        }

        ItemDao dao = new ItemDao();
        Map<String, Object> results = dao.updateProjectTaskForItem(projectTask, itemId);
        PutProjectTaskForRequestItemsResponse putProjectTaskForRequestItemsResponse = new PutProjectTaskForRequestItemsResponse();
        StatusCode statusCode = (StatusCode) results.get(ItemDao.STATUS_CODE_KEY);
        if (statusCode == StatusCode.OK) {
            putProjectTaskForRequestItemsResponse.setRowsUpdated((Integer) results.get(ItemDao.ROW_COUNT_KEY));
        }

        return Response.ok().entity(serializeResponseWithStatus(putProjectTaskForRequestItemsResponse, statusCode)).build();
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{itemId}/objectClass/{objectClass}")
    public Response putObjectClassForRequestItems(@Context HttpServletRequest servletRequest, @PathParam("itemId") Integer itemId, @PathParam("objectClass") String objectClass) {
        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
        }

        ItemDao dao = new ItemDao();
        Map<String, Object> results = dao.updateObjectClassForItem(objectClass, itemId);
        PutObjectClassForRequestItemsResponse putObjectClassForRequestItemsResponse = new PutObjectClassForRequestItemsResponse();
        StatusCode statusCode = (StatusCode) results.get(ItemDao.STATUS_CODE_KEY);
        if (statusCode == StatusCode.OK) {
            putObjectClassForRequestItemsResponse.setRowsUpdated((Integer) results.get(ItemDao.ROW_COUNT_KEY));
        }

        return Response.ok().entity(serializeResponseWithStatus(putObjectClassForRequestItemsResponse, statusCode)).build();
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{itemId}/process")
    public Response putItemProcess(@Context HttpServletRequest servletRequest, @PathParam("itemId") Integer itemId, PutItemProcessRequest putItemProcessRequest) {
        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
        }

        String[] roles = {"Bankcard Holder", "Bankcard Approving Official"};
        if (!isUserInRole(roles, authenticatedUser.getPeopleId())) {
            LOG.info("User doesn't have role");
            return Response.status(Response.Status.OK).entity(serializeStatus(StatusCode.InsufficienPrivileges)).build();
        }

        Item item = new Item();
        item.setId(itemId);
        item.setPrice(putItemProcessRequest.getPrice());
        item.setQuantity(putItemProcessRequest.getQuantity());
        item.setActualPrice(putItemProcessRequest.getActualPrice());
        item.setActualQuantity(putItemProcessRequest.getActualQuantity());
        item.setItemNotes(putItemProcessRequest.getItemNotes());
        item.setDateReceived(putItemProcessRequest.getDateReceived());
        ItemDao dao = new ItemDao();
        Map<String, Object> results = dao.updateItemForProcessedRequest(item, putItemProcessRequest.getItemStatusTypeId(), authenticatedUser.getPeopleId());

        PutItemProcessResponse putItemProcessResponse = new PutItemProcessResponse();
        StatusCode statusCode = (StatusCode) results.get(ItemDao.STATUS_CODE_KEY);
        if (statusCode == StatusCode.OK) {
            putItemProcessResponse.setInProcessState((Boolean) results.get(ItemDao.PROCESSED_STATE_KEY));
            putItemProcessResponse.setRowCount((Integer) results.get(ItemDao.ROW_COUNT_KEY));
        }

        return Response.ok().entity(serializeResponseWithStatus(putItemProcessResponse, statusCode)).build();
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{itemId}/billed")
    public Response putItemBilled(@Context HttpServletRequest servletRequest, @PathParam("itemId") Integer itemId, PutItemBilledRequest putItemBilledRequest) {
        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
        }

        String[] roles = {"Bankcard Holder"};
        if (!isUserInRole(roles, authenticatedUser.getPeopleId())) {
            LOG.info("User doesn't have role");
            return Response.status(Response.Status.OK).entity(serializeStatus(StatusCode.InsufficienPrivileges)).build();
        }

        Item item = new Item();
        item.setId(itemId);
        item.setTransactionNumber(putItemBilledRequest.getTransactionNumber());
        item.setStatementDate(putItemBilledRequest.getStatementDate());
        ItemDao dao = new ItemDao();
        Map<String, Object> results = dao.updateItemForBilledRequest(item);

        PutItemBilledResponse putItemBilledResponse = new PutItemBilledResponse();
        StatusCode statusCode = (StatusCode) results.get(ItemDao.STATUS_CODE_KEY);
        if (statusCode == StatusCode.OK) {
            putItemBilledResponse.setRowCount((Integer) results.get(ItemDao.ROW_COUNT_KEY));
        }

        return Response.ok().entity(serializeResponseWithStatus(putItemBilledResponse, statusCode)).build();
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{itemId}/partialDelivery")
    public Response putPartialDelivery(@Context HttpServletRequest servletRequest, @PathParam("itemId") Integer itemId, PutPartialDeliveryRequest putPartialDeliveryRequest) {
        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
        }

        ItemDao dao = new ItemDao();
        Map<String, Object> results = dao.callPartialDelivery(itemId, putPartialDeliveryRequest.getDeliveredQuantity(), authenticatedUser.getPeopleId());
        StatusCode statusCode = (StatusCode) results.get(ItemDao.STATUS_CODE_KEY);

        PutPartialDeliveryResponse putPartialDeliveryResponse = new PutPartialDeliveryResponse();
        if (results.containsKey(ItemDao.ERROR_CODE_KEY)) {
            Error error = new Error();
            putPartialDeliveryResponse.setError(error);
            putPartialDeliveryResponse.getError().setCode((Integer) results.get(ItemDao.ERROR_CODE_KEY));
            putPartialDeliveryResponse.getError().setDescription((String) results.get(ItemDao.ERROR_MESSAGE_KEY));
            putPartialDeliveryResponse.setSuccess(false);
        }

        // Do a custom error message instead of the default.
        if (putPartialDeliveryResponse.getError() != null) {
            return Response.ok().entity(putPartialDeliveryResponse).build();
        }

        return Response.ok().entity(serializeResponseWithStatus(putPartialDeliveryResponse, statusCode)).build();
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("/{item_id}")
    public Response deleteItem(@Context HttpServletRequest servletRequest, @PathParam("item_id") Integer itemId, @FormParam("fileId") Integer fileId) {
        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
        }

        ItemDao dao = new ItemDao();
        Map<String, Object> results = dao.deleteItem(itemId, fileId);
        StatusCode statusCode = (StatusCode) results.get(ItemDao.STATUS_CODE_KEY);
        DeleteItemResponse deleteItemResponse = new DeleteItemResponse();

        return Response.ok().entity(serializeResponseWithStatus(deleteItemResponse, statusCode)).build();
    }

    private ChemicalItem setChemicalItem(PutOrPostItemRequest putOrPostItemRequest) {
        ChemicalItem chemicalItem = new ChemicalItem();
        if (putOrPostItemRequest.getChemicalData() != null) {
            PutOrPostItemRequest.ChemicalData chemicalData = putOrPostItemRequest.getChemicalData();
            chemicalItem.setOwnerId(chemicalData.getChemicalOwnerId());
            chemicalItem.setLocation(chemicalData.getLocationOfChemical());
            chemicalItem.setSubLocation(chemicalData.getSubLocation());
            chemicalItem.setCasNumber(chemicalData.getCasNumber());
            chemicalItem.setChemicalForm(chemicalData.getChemicalForm());
            chemicalItem.setChemicalGrade(chemicalData.getChemicalGrade());
            chemicalItem.setManufacturerName(chemicalData.getManufacturerName());
            chemicalItem.setChemicalCatalogNumber(chemicalData.getChemicalCatalogNumber());
            chemicalItem.setCatalogNumberQuantity(chemicalData.getQuantityOfCatalogNumber());
            chemicalItem.setContainersPerPackage(chemicalData.getContainersPerPackage());
            chemicalItem.setAmountPerContainer(chemicalData.getAmountAndUnitPerContainer());
            chemicalItem.setLabelsNeeded(chemicalData.getNumOfLabelsOrBarcodesNeeded());
            chemicalItem.setContainerType(chemicalData.getContainerType());
            chemicalItem.setContainerTotal(chemicalData.getContainerTotal());
            chemicalItem.setProductUrl(chemicalData.getProductUrl());
            CommonUtil.setDateFromString(chemicalData.getExpirationDate(), chemicalItem::setExpirationDate, "expirationDate");
            chemicalItem.setHealthNfpaValue(chemicalData.getHealthNfpaValue());
            chemicalItem.setFlammabilityNpfaValue(chemicalData.getFlammabilityNfpaValue());
            chemicalItem.setReactivityNpfaValue(chemicalData.getReactivityNfpaValue());
            chemicalItem.setSpecialCodeNpfaValue(chemicalData.getSpecialCodeNfpaValue());
            chemicalItem.setIsRadioactiveMaterial(chemicalData.getIsRadioactiveMaterial());
            chemicalItem.setBiohazardRegistrationRequired(chemicalData.getBiohazardRegistrationRequired());
            chemicalItem.setSpecialInstructions(chemicalData.getSpecialInstructions());
            chemicalItem.setIbbrRoomId(chemicalData.getIbbrRoomId());
            chemicalItem.setIbbrRoomName(chemicalData.getIbbrRoomName());
            chemicalItem.setPrimaryUserId(chemicalData.getPrimaryUserId());
            chemicalItem.setCisproRemarks(chemicalData.getCisproRemarks());
        }
        return chemicalItem;
    }

    private List<NistOrgData> getSupportedDivisions(Integer peopleId) {
        String supportedDivisionsWsUrl = ApiUtil.getMmlSupportedDivisionsUrl(peopleId);
        NistOrgWSCalls.MmlSupportedDivisionsUrlCall mmlSupportedDivisionsUrlCall = null;
        try {
            mmlSupportedDivisionsUrlCall = NistOrgWSCalls.callMmlSupportedDivisionsService(supportedDivisionsWsUrl);
        } catch (Exception caught) {
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
            return null;
        }

        if (mmlSupportedDivisionsUrlCall == null || !"success".equals(mmlSupportedDivisionsUrlCall.returnMessage)) {
            return new ArrayList<>(); // Return empty list if call fails or is not successful
        }

        List<NistOrgData> dataList = mmlSupportedDivisionsUrlCall.divisions.stream()
                .map(div -> {
                    NistOrgData nistOrgData = new NistOrgData();
                    nistOrgData.setOuId(Integer.parseInt(div.organization_unit_id));
                    nistOrgData.setDivisionId(div.id);
                    nistOrgData.setCode(div.code);
                    nistOrgData.setName(div.name);
                    return nistOrgData;
                })
                .collect(Collectors.toList());

        // Add "All Supported Divisions" as the first element
        NistOrgData allDivisions = new NistOrgData();
        allDivisions.setOuId(0);
        allDivisions.setDivisionId(0);
        allDivisions.setCode("0");
        allDivisions.setName("All Supported Divisions");
        dataList.add(0, allDivisions); // Add at the beginning

        return dataList;
    }

    private GetShoppingCartItemResponse populateGetShoppingCartItemResponse(Item item, FileAttachment fileAttachment) {
        GetShoppingCartItemResponse getShoppingCartItemResponse = new GetShoppingCartItemResponse();
        if (item != null && fileAttachment != null) {
            GetShoppingCartItemResponse.Item data = new GetShoppingCartItemResponse.Item();
            data.setItemId(item.getId());
            data.setRequestId(data.getRequestId());
            //data.setItemType(item.getType());
            data.setVendorId(item.getVendorId());
            data.setCatalogNumber(item.getCatalogNumber());
            data.setItemName(item.getItemName());
            data.setItemDescription(item.getDescription());
            data.setPrice(item.getPrice());
            data.setQuantity(item.getQuantity());
            data.setPurpose(item.getPurpose());
            data.setChemical(item.getChemical());
            data.isTaggableEquipment = item.getIsTaggableEquipment();
            data.setProjectTask(item.getProjectTask());
            data.setShoppingCartFileId(item.getShoppingCartFileId());
            data.setItemStatusId(item.getStatusId());
            data.setObjectClass(item.getObjectClass());
            data.setFileId(fileAttachment.getId());
            data.setFileCategoryId(fileAttachment.getCategoryId());
            data.setFileCategoryName(fileAttachment.getCategoryName());
            data.setFileName(fileAttachment.getName());
            data.setFileTypeCode(fileAttachment.getTypeCode());
            data.setFileSize(fileAttachment.getSize());
            data.setCreatedBy(fileAttachment.getCreatedBy());
            data.setCreatedByName(fileAttachment.getCreatedByName());
            data.setLatestItemStatusTypeId(item.getLatestStatusTypeId());
            data.setLatestItemStatusTypeName(item.getLatestStatusTypeName());
            data.setIsShippingCost(item.getIsShipping());
            data.setItemNotes(item.getItemNotes());
            data.setDateReceived(item.getDateReceived());
            data.setTransactionNumber(item.getTransactionNumber());
            data.setStatementDate(item.getStatementDate());

            getShoppingCartItemResponse.setData(data);
        }

        return getShoppingCartItemResponse;
    }

    private GetChemicalItemResponse populateGetChemicalItemResponse(Item item, ChemicalItem chemicalItem) {
        GetChemicalItemResponse getChemicalItemResponse = new GetChemicalItemResponse();
        if (item != null && chemicalItem != null) {
            GetChemicalItemResponse.Item data = new GetChemicalItemResponse.Item();
            data.setItemId(item.getId());
            data.setRequestId(data.getRequestId());
            //data.setItemType(item.getType());
            data.setVendorId(item.getVendorId());
            data.setCatalogNumber(item.getCatalogNumber());
            data.setItemName(item.getItemName());
            data.setItemDescription(item.getDescription());
            data.setPrice(item.getPrice());
            data.setQuantity(item.getQuantity());
            data.setPurpose(item.getPurpose());
            data.setChemical(item.getChemical());
            data.isTaggableEquipment = item.getIsTaggableEquipment();
            data.setProjectTask(item.getProjectTask());
            data.setShoppingCartFileId(item.getShoppingCartFileId());
            data.setItemStatusId(item.getStatusId());
            data.setObjectClass(item.getObjectClass());
            data.setItemNotes(item.getItemNotes());
            data.setDateReceived(item.getDateReceived());
            data.setOwnerId(chemicalItem.getOwnerId());
            data.setLocation(chemicalItem.getLocation());
            data.setSubLocation(chemicalItem.getSubLocation());
            data.setCasNumber(chemicalItem.getCasNumber());
            data.setChemicalForm(chemicalItem.getChemicalForm());
            data.setChemicalGrade(chemicalItem.getChemicalGrade());
            data.setManufacturerName(chemicalItem.getManufacturerName());
            data.setChemicalCatalogNumber(chemicalItem.getChemicalCatalogNumber());
            data.setCatalogNumberQuantity(chemicalItem.getCatalogNumberQuantity());
            data.setContainersPerPackage(chemicalItem.getContainersPerPackage());
            data.setAmountPerContainer(chemicalItem.getAmountPerContainer());
            data.setLabelsNeeded(chemicalItem.getLabelsNeeded());
            data.setContainerType(chemicalItem.getContainerType());
            data.setExpirationDate(chemicalItem.getExpirationDate());
            data.setHealthNfpaValue(chemicalItem.getHealthNfpaValue());
            data.setFlammabilityNpfaValue(chemicalItem.getFlammabilityNpfaValue());
            data.setReactivityNpfaValue(chemicalItem.getReactivityNpfaValue());
            data.setSpecialCodeNpfaValue(chemicalItem.getSpecialCodeNpfaValue());
            data.setIsRadioactiveMaterial(chemicalItem.getIsRadioactiveMaterial());
            data.setBiohazardRegistrationRequired(chemicalItem.getBiohazardRegistrationRequired());
            data.setSpecialInstructions(chemicalItem.getSpecialInstructions());
            data.setIbbrRoomId(chemicalItem.getIbbrRoomId());
            data.setIbbrRoomName(chemicalItem.getIbbrRoomName());
            data.setPrimaryUserId(chemicalItem.getPrimaryUserId());
            data.setPrimaryUserName(chemicalItem.getPrimaryUserName());
            data.setCisproRemarks(chemicalItem.getCisproRemarks());
            data.setLatestItemStatusTypeId(item.getLatestStatusTypeId());
            data.setLatestItemStatusTypeName(item.getLatestStatusTypeName());
            data.setIsShippingCost(item.getIsShipping());
            data.setTransactionNumber(item.getTransactionNumber());
            data.setStatementDate(item.getStatementDate());
            data.setContainerTotal(chemicalItem.getContainerTotal());
            data.setProductUrl(chemicalItem.getProductUrl());

            getChemicalItemResponse.setData(data);
        }

        return getChemicalItemResponse;
    }

    private GetItemResponse populateGetItemResponse(Item item) {
        GetItemResponse getItemResponse = new GetItemResponse();
        if (item != null) {
            GetItemResponse.Item data = new GetItemResponse.Item();
            data.setItemId(item.getId());
            data.setRequestId(data.getRequestId());
            // data.setItemType(item.getType());
            data.setVendorId(item.getVendorId());
            data.setCatalogNumber(item.getCatalogNumber());
            data.setItemName(item.getItemName());
            data.setItemDescription(item.getDescription());
            data.setPrice(item.getPrice());
            data.setQuantity(item.getQuantity());
            data.setActualPrice(item.getActualPrice());
            data.setActualQuantity(item.getActualQuantity());
            data.setPurpose(item.getPurpose());
            data.setChemical(item.getChemical());
            data.setIsTaggableEquipment(item.getIsTaggableEquipment());
            data.setProjectTask(item.getProjectTask());
            data.setShoppingCartFileId(item.getShoppingCartFileId());
            data.setItemStatusId(item.getStatusId());
            data.setObjectClass(item.getObjectClass());
            data.setLatestItemStatusTypeId(item.getLatestStatusTypeId());
            data.setLatestItemStatusTypeName(item.getLatestStatusTypeName());
            data.setIsShippingCost(item.getIsShipping());
            data.setItemNotes(item.getItemNotes());
            data.setUnitIssue(item.getUnitIssue());
            data.setDateReceived(item.getDateReceived());
            data.setTransactionNumber(item.getTransactionNumber());
            data.setStatementDate(item.getStatementDate());

            getItemResponse.setData(data);
        }

        return getItemResponse;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class GetItemResponse extends JsonStatus {

        @Data
        public static class Item {

            private Integer itemId;
            private Integer requestId;
            private String itemType;
            private Integer vendorId;
            private String catalogNumber;
            private String itemName;
            private String itemDescription;
            private Double price;
            private Integer quantity;
            private Double actualPrice;
            private Integer actualQuantity;
            private String purpose;
            private Boolean chemical;
            public Boolean isTaggableEquipment;
            private String projectTask;
            private Integer shoppingCartFileId;
            private Integer itemStatusId;
            @XmlElement(nillable = true)
            private String objectClass;
            private Integer latestItemStatusTypeId;
            private String latestItemStatusTypeName;
            private Boolean isShippingCost;
            private String itemNotes;
            private String unitIssue;
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
            private Date dateReceived;
            private String transactionNumber;
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
            private Date statementDate;
        }
        @XmlElement(nillable = true)
        private GetItemResponse.Item data;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class GetChemicalItemResponse extends JsonStatus {

        @Data
        public static class Item {

            private Integer itemId;
            private Integer requestId;
            private String itemType;
            private Integer vendorId;
            private String catalogNumber;
            private String itemName;
            private String itemDescription;
            private Double price;
            private Integer quantity;
            private String purpose;
            private Boolean chemical;
            public Boolean isTaggableEquipment;
            private String projectTask;
            private Integer shoppingCartFileId;
            private Integer itemStatusId;
            private String objectClass;
            private String itemNotes;
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
            private Date dateReceived;
            private Boolean isShippingCost;
            private Integer ownerId;
            private String location;
            private String subLocation;
            private String casNumber;
            private String chemicalForm;
            private String chemicalGrade;
            private String manufacturerName;
            private String chemicalCatalogNumber;
            private String catalogNumberQuantity;
            private String containersPerPackage;
            private String amountPerContainer;
            private Integer labelsNeeded;
            private String containerType;
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
            private Date expirationDate;
            private String healthNfpaValue;
            private String flammabilityNpfaValue;
            private String reactivityNpfaValue;
            private String specialCodeNpfaValue;
            private Boolean isRadioactiveMaterial;
            private Boolean biohazardRegistrationRequired;
            private String specialInstructions;
            private Integer latestItemStatusTypeId;
            private String latestItemStatusTypeName;
            private Integer ibbrRoomId;
            private String ibbrRoomName;
            private Integer primaryUserId;
            private String primaryUserName;
            private String cisproRemarks;
            private String transactionNumber;
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
            private Date statementDate;
            private int containerTotal;
            private String productUrl;
        }
        @XmlElement(nillable = true)
        private GetChemicalItemResponse.Item data;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class GetShoppingCartItemResponse extends JsonStatus {

        @Data
        public static class Item {

            private Integer itemId;
            private Integer requestId;
            private String itemType;
            private Integer vendorId;
            private String catalogNumber;
            private String itemName;
            private String itemDescription;
            private Double price;
            private Integer quantity;
            private String purpose;
            private Boolean chemical;
            public Boolean isTaggableEquipment;
            private String projectTask;
            private Integer shoppingCartFileId;
            private Integer itemStatusId;
            private String objectClass;
            private Integer fileId;
            private Integer fileCategoryId;
            private String fileCategoryName;
            private String fileName;
            private String fileTypeCode;
            private Integer fileSize;
            private Integer createdBy;
            private String createdByName;
            private Integer latestItemStatusTypeId;
            private String latestItemStatusTypeName;
            private Boolean isShippingCost;
            private String itemNotes;
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
            private Date dateReceived;
            private String transactionNumber;
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
            private Date statementDate;
        }

        @XmlElement(nillable = true)
        private GetShoppingCartItemResponse.Item data;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PutOrPostItemRequest {

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class ChemicalData {

            private Integer itemId;
            private Integer chemicalOwnerId;
            private Integer primaryUserId;
            private Integer numOfLabelsOrBarcodesNeeded;
            private String expirationDate;
            private Boolean isRadioactiveMaterial;
            private Boolean biohazardRegistrationRequired;
            private String specialInstructions;
            private String cisproRemarks;
            private String id;
            private String locationOfChemical;
            private String subLocation;
            private String chemicalForm;
            private String casNumber;
            private String chemicalGrade;
            private String manufacturerName;
            private String chemicalCatalogNumber;
            private String quantityOfCatalogNumber;
            private String containersPerPackage;
            private String amountAndUnitPerContainer;
            private String containerType;
            private String healthNfpaValue;
            private String flammabilityNfpaValue;
            private String reactivityNfpaValue;
            private String specialCodeNfpaValue;
            private Integer ibbrRoomId;
            private String ibbrRoomName;
            private int containerTotal;
            private String productUrl;
        }

        private Integer id;
        private Integer itemId;
        private String unitIssue;
        private Integer requestId;
        private Integer vendorId;
        private String vendorName;
        private String itemName;
        private String description;
        private String catalogNumber;
        private String purpose;
        private Double unitPrice;
        private Integer quantity;
        private Double amount;
        private Boolean isChemical;
        public Boolean isTaggableEquipment;
        private String objectClass;
        private String projTask;
        private Integer fileId;
        private Boolean isShippingCost;
        private String itemNotes;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
        private Date dateReceived;
        private String createdByName;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
        private Date createdDate;
        private String updatedByName;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
        private Date updatedDate;
        private String transactionNumber;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
        private Date statementDate;
        private ChemicalData chemicalData;

        // Custom setter for itemName to sanitize it
        public void setItemName(String itemName) {
            this.itemName = StringUtil.sanitizeString(itemName);
        }

        // Custom setter for description to sanitize it
        public void setDescription(String description) {
            this.description = StringUtil.sanitizeString(description);
        }

        // Custom setter for purpose to sanitize it
        public void setPurpose(String purpose) {
            this.purpose = StringUtil.sanitizeString(purpose);
        }
    }

    @Getter
    @Setter
    public static class PutProjectTaskForRequestItemsResponse extends JsonStatus {

        private Integer rowsUpdated;
    }

    @Getter
    @Setter
    public static class PutObjectClassForRequestItemsResponse extends JsonStatus {

        private Integer rowsUpdated;
    }

    @Getter
    @Setter
    public static class PostItemResponse extends JsonStatus {

        private Integer itemId;
    }

    @Data
    public static class PostShoppingCartResponse {

        private boolean success;
        private Integer itemId;
        private Integer fileId;
        private Integer code;
        private String description;

        @Override
        public String toString() {
            StringBuilder output = new StringBuilder("{ ");
            output.append(String.format("\"success\": %s, ", success ? "true" : "false"));
            if (success) {
                if (fileId != null && itemId != null) {
                    output.append(String.format("\"itemId\": %d, \"fileId\": %d", itemId, fileId));
                }
            } else if (code != null && description != null) {
                output.append(String.format("\"code\": %d, \"description\": \"%s\"", code, description));
            }
            output.append(" }");
            return output.toString();
        }
    }

    @Getter
    @Setter
    public static class PostCsvItemResponse extends JsonStatus {

        private Integer itemId;
    }

    @Getter
    @Setter
    public static class PutItemResponse extends JsonStatus {
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PutItemProcessRequest {

        private Double price;
        private Integer quantity;
        private Double actualPrice;
        private Integer actualQuantity;
        private Integer itemStatusTypeId;
        private String itemNotes;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
        private Date dateReceived;
    }

    @Getter
    @Setter
    public static class PutItemProcessResponse extends JsonStatus {

        private Boolean inProcessState;
        private Integer rowCount;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PutItemBilledRequest {

        private String transactionNumber;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
        private Date statementDate;
    }

    @Getter
    @Setter
    public static class PutItemBilledResponse extends JsonStatus {

        private Integer rowCount;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PutPartialDeliveryRequest {

        @Getter
        @Setter
        private Integer deliveredQuantity;
    }

    public static class PutPartialDeliveryResponse extends JsonStatus {
    }

    public static class DeleteItemResponse extends JsonStatus {
    }

    public static class GetItemStatusTypeResponse extends JsonStatus {

        @Getter
        @Setter
        private List<Lookup> data;

    }

    public static class GetEAItemByCriteriaResponse extends JsonStatus {

        @Getter
        @Setter
        private List<EaItem> data;

    }

    public static class GetPcItemByCriteriaResponse extends JsonStatus {

        @Getter
        @Setter
        private List<PcItem> data;

    }

    public static class GetSupportedDivisionsResponse extends JsonStatus {

        @Getter
        @Setter
        private List<NistOrgData> data;

    }
}
