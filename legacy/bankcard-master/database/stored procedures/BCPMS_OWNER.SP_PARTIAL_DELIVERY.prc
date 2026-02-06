CREATE OR REPLACE PROCEDURE BCPMS_OWNER.sp_partial_delivery (
    p_item_id              IN INTEGER,
    p_delivered_quantity   IN INTEGER,
    p_user_id              IN INTEGER)
IS
    l_original_quantity      INTEGER;
    l_undelivered_quantity   INTEGER;
    new_item_id              INTEGER;
    l_is_chemical            CHAR (1 BYTE);
    l_is_shipping            CHAR (1 BYTE);
    l_cart_file_id           INTEGER;
BEGIN
    l_original_quantity := 0;
    l_undelivered_quantity := 0;
    new_item_id := 0;


    SELECT QUANTITY,
           CHEMICAL,
           IS_SHIPPING,
           SHOPPING_CART_FILE_ID
      INTO l_original_quantity,
           l_is_chemical,
           l_is_shipping,
           l_cart_file_id
      FROM ITEM
     WHERE item_id = p_item_id;


    IF l_is_shipping = 'Y'
    THEN
        raise_application_error (
            -20201,
            'partial delivery cannot be applied on shipping cost');
    END IF;

    IF l_cart_file_id IS NOT NULL
    THEN
        raise_application_error (
            -20202,
            'partial delivery cannot be applied on Shopping Cart Item');
    END IF;

    IF l_original_quantity <= p_delivered_quantity
    THEN
        raise_application_error (
            -20203,
            'delivered quantity need to be less than the quantity of the original item');
    END IF;

    l_undelivered_quantity := l_original_quantity - p_delivered_quantity;

    SELECT SEQ_ITEM_ID.NEXTVAL INTO new_item_id FROM DUAL;

    --insert a new item with delivered quantity and a note
    INSERT INTO ITEM (ITEM_ID,
                      REQUEST_ID,
                      ITEM_TYPE,
                      VENDOR_ID,
                      CATELOG_NUMBER,
                      ITEM_NAME,
                      ITEM_DESCRIPTION,
                      PRICE,
                      QUANTITY,
                      PURPOSE,
                      CHEMICAL,
                      PROJECT_TASK,
                      SHOPPING_CART_FILE_ID,
                      ITEM_STATUS_ID,
                      OBJECT_CLASS,
                      IS_PRECIOUS_METAL,
                      IS_SHIPPING,
                      PRICE_ORDERED,
                      QUANTITY_ORDERED,
                      ITEM_NOTES,
                      DATE_RECEIVED,
                      TRANSACTION_NUMBER,
                      STATEMENT_DATE)
        (SELECT new_item_id,
                REQUEST_ID,
                ITEM_TYPE,
                VENDOR_ID,
                CATELOG_NUMBER,
                ITEM_NAME,
                ITEM_DESCRIPTION,
                PRICE,
                p_delivered_quantity,
                PURPOSE,
                CHEMICAL,
                PROJECT_TASK,
                SHOPPING_CART_FILE_ID,
                ITEM_STATUS_ID,
                OBJECT_CLASS,
                IS_PRECIOUS_METAL,
                IS_SHIPPING,
                PRICE_ORDERED,
                p_delivered_quantity,
                'Partial Delivery from Item ' || p_item_id,
                DATE_RECEIVED,
                TRANSACTION_NUMBER,
                STATEMENT_DATE
           FROM ITEM
          WHERE item_id = p_item_id);


    --insert a delivered item status for the delivered item
    INSERT INTO ITEM_STATUS (ITEM_ID,
                             ITEM_STATUS_TYPE_ID,
                             ITEM_STATUS_NOTES,
                             CREATED_BY,
                             CREATED_DATE)
         VALUES (new_item_id,
                 3,
                 'generated from partial delivery function',
                 p_user_id,
                 SYSDATE);

    --if the item is a chemical item, need to make a copy of the chemcial item also
    IF l_is_chemical = 'Y'
    THEN
        INSERT INTO ITEM_CHEMICAL (ITEM_ID,
                                   OWNER_ID,
                                   LOCATION,
                                   SUB_LOCATION,
                                   CAS_NUMBER,
                                   CHEMICAL_FORM,
                                   CHEMICAL_GRADE,
                                   MANUFACTURER_NAME,
                                   CATALOG_NUMBER,
                                   CATALOG_NUMBER_QUANTITY,
                                   CONTAINERS_PER_PACKAGE,
                                   AMOUNT_PER_CONTAINER,
                                   LABLES_NEEDED,
                                   CONTAINER_TYPE,
                                   EXPIRATION_DATE,
                                   HEALTH_NFPA_VALUE,
                                   FLAMMABILITY_NFPA_VALUE,
                                   REACTIVITY_NFPA_VALUE,
                                   SPECIAL_CODE_NFPA_VALUE,
                                   IS_RADIOACTIVE_MATERIAL,
                                   BIOHAZARD_REGISTRATION_REQ,
                                   SPECIAL_INSTRUCTION,
                                   IBBR_ROOM_ID,
                                   IBBR_ROOM_NAME,
                                   PRIMARY_USER_ID,
                                   CISPRO_REMARKS,
                                   CONTAINER_TOTAL,
                                   PRODUCT_URL)
            SELECT new_item_id,
                   OWNER_ID,
                   LOCATION,
                   SUB_LOCATION,
                   CAS_NUMBER,
                   CHEMICAL_FORM,
                   CHEMICAL_GRADE,
                   MANUFACTURER_NAME,
                   CATALOG_NUMBER,
                   CATALOG_NUMBER_QUANTITY,
                   CONTAINERS_PER_PACKAGE,
                   AMOUNT_PER_CONTAINER,
                   LABLES_NEEDED,
                   CONTAINER_TYPE,
                   EXPIRATION_DATE,
                   HEALTH_NFPA_VALUE,
                   FLAMMABILITY_NFPA_VALUE,
                   REACTIVITY_NFPA_VALUE,
                   SPECIAL_CODE_NFPA_VALUE,
                   IS_RADIOACTIVE_MATERIAL,
                   BIOHAZARD_REGISTRATION_REQ,
                   SPECIAL_INSTRUCTION,
                   IBBR_ROOM_ID,
                   IBBR_ROOM_NAME,
                   PRIMARY_USER_ID,
                   CISPRO_REMARKS,
                   CONTAINER_TOTAL,
                   PRODUCT_URL
              FROM ITEM_CHEMICAL
             WHERE item_id = p_item_id;
    END IF;


    --update original item's quantity
    UPDATE ITEM
       SET quantity = l_undelivered_quantity,
           quantity_ordered = l_undelivered_quantity
     WHERE item_id = p_item_id;
END;
/



/* Formatted on 6/28/2018 4:37:42 PM (QP5 v5.256.13226.35538) */
/


CREATE OR REPLACE SYNONYM BCPMS_APP.SP_PARTIAL_DELIVERY FOR BCPMS_OWNER.SP_PARTIAL_DELIVERY;


AUDIT RENAME ON BCPMS_OWNER.SP_PARTIAL_DELIVERY BY ACCESS WHENEVER SUCCESSFUL;
AUDIT RENAME ON BCPMS_OWNER.SP_PARTIAL_DELIVERY BY ACCESS WHENEVER NOT SUCCESSFUL;

GRANT EXECUTE ON BCPMS_OWNER.SP_PARTIAL_DELIVERY TO BCPMS_APP;