
package gov.nist.oism.asd.empbc.util;

import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
/**
 *
 * @author xinweiw
 */
public class PdfUtils {
    /**
     * Sets all fields in the given PDAcroForm to read-only.
     *
     * @param acroForm the PDAcroForm to modify
     */
    public static void setAllFieldsReadOnly(PDAcroForm acroForm) {
        if (acroForm != null) {
            // Loop through all fields in the AcroForm
            for (PDField field : acroForm.getFields()) {
                // Set the field to read-only
                field.setReadOnly(true);
            }
        } else {
            System.out.println("The provided PDAcroForm is null.");
        }
    }
    
    
}
