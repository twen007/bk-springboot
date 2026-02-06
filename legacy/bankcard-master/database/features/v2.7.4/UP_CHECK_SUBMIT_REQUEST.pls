CREATE OR REPLACE PROCEDURE BCPMS_OWNER.UP_check_submit_request (
    p_request_id       INT,
    p_rc           OUT NVARCHAR2,
    p_pc_div       OUT NVARCHAR2)
IS
    TYPE v_just_type IS TABLE OF request_justification%ROWTYPE;

    v_justification   v_just_type;

    TYPE v_item_type IS TABLE OF item%ROWTYPE;

    v_item            v_item_type;

    v_req_num         request.requisition_number%TYPE;
    v_org_cd          nist_group.org_cd%TYPE;
    v_request_ct      INT;
    v_has_chemical    CHAR (1) := 'N';
    v_has_taggable_equipment    CHAR (1) := 'N';
    v_codes           VARCHAR2 (10);
    v_requester_id    INTEGER;
    v_pc_div          VARCHAR2 (3);
    v_ou_id           NUMBER (6);
    v_div_id          NUMBER (6);
    v_grp_id          NUMBER (6);
    l_div_id             NUMBER (6);
    l_justification_preference VARCHAR2 (1);
    l_finance_preference VARCHAR2 (1);
   -- l_rc           OUT NVARCHAR2;
    
BEGIN

    l_div_id:=0;
    p_rc := '';
    p_pc_div := '';

    -- check if the request is exists
    SELECT COUNT (*)
      INTO v_request_ct
      FROM request
     WHERE request_id = p_request_id;

    IF v_request_ct = 0
    THEN
        p_rc := 'No request found';
        RETURN;
    END IF;

        --find who is the official requester
        SELECT requester_id
          INTO v_requester_id
          FROM request
         WHERE request_id = p_request_id;

        --find org data for the requester
        SELECT SUBSTR (b.ORG_CD, 0, 3),
               b.OU_ORG_ID,
               b.DIV_ORG_ID,
               b.ORG_ID
          INTO v_pc_div,
               v_ou_id,
               v_div_id,
               v_grp_id
          FROM nist_user_active a, nist_group b
         WHERE  a.GRP_ORG_ID=b.ORG_ID
         AND a.DIV_ORG_ID = b.DIV_ORG_ID 
         AND a.PEOPLE_ID = v_requester_id;
    
     
    --get division preferences for justification and finance data
    select just_pref_val, finance_pref_val into l_justification_preference,l_finance_preference
    from div_preferences
    where div_id=v_div_id;
    
    --decide wether justification and finance validation is required
    if l_justification_preference = 'Y'
    then
        UP_CHECK_PROCESS_REQUEST(p_request_id,p_rc);
    end if;

    -- check for items
    SELECT *
      BULK COLLECT INTO v_item
      FROM item
     WHERE request_id = p_request_id;

    IF v_item.COUNT = 0
    THEN
        p_rc :=
               p_rc
            || 'The request does not contain any item. '
            || CHR (13)
            || CHR (10);
    ELSE
        FOR indx2 IN 1 .. v_item.COUNT
        LOOP
            IF v_item (indx2).price IS NULL
            THEN
                p_rc :=
                       p_rc
                    || 'Unit price is missing for item: '
                    || TO_CHAR (v_item (indx2).item_id)
                    || '-'
                    || TO_CHAR (v_item (indx2).item_name)
                    || '.'
                    || CHR (13)
                    || CHR (10);
            END IF;

            IF v_item (indx2).quantity IS NULL
            THEN
                p_rc :=
                       p_rc
                    || 'Quantity is missing for item: '
                    || TO_CHAR (v_item (indx2).item_id)
                    || '-'
                    || TO_CHAR (v_item (indx2).item_name)
                    || '.'
                    || CHR (13)
                    || CHR (10);
            END IF;

            IF v_has_chemical = 'N'
            THEN
                v_has_chemical := v_item (indx2).chemical;
            END IF;
            
            IF v_has_taggable_equipment = 'N'
            THEN
                v_has_taggable_equipment := v_item (indx2).IS_TAGGABLE_EQUIPMENT;
            END IF;
            
        /* 2017-07-14 comment out.
                    if v_item(indx2).project_task is null then
                       p_rc := p_rc || 'Project task is missing for item: ' || to_char(v_item(indx2).item_id) || '-' || to_char(v_item(indx2).item_name) || '.' || chr(13) || chr(10);
                    end if;
                    if v_item(indx2).object_class is null then
                       p_rc := p_rc || 'Object class is missing for item: ' || to_char(v_item(indx2).item_id) || '-' || to_char(v_item(indx2).item_name) || '.' || chr(13) || chr(10);
                    end if;
        */
        END LOOP;
    END IF;

    IF LENGTH (p_rc) IS NULL
    THEN
        --check requisition number
        /**remove this logic becuase the req num is generated in admin portal now
         --the check req num logic is now in the server code when trying to insert a route with status =1 submitted
        SELECT requisition_number
          INTO v_req_num
          FROM request
         WHERE request_id = p_request_id;

        -- if not find, create
        IF (v_req_num IS NULL OR v_req_num = '')
        THEN
           SELECT div.org_cd
             INTO v_org_cd
             FROM request r
                  INNER JOIN nist_user u ON r.requester_id = u.people_id
                  INNER JOIN nist_division div ON u.div_org_id = div.org_id
            WHERE r.request_id = p_request_id;

           up_get_requisition_number (v_org_cd, v_req_num);

           UPDATE request
              SET requisition_number = v_req_num
            WHERE request_id = p_request_id;

           COMMIT;
        END IF;
  **/

        

        --update request with requester's org data
        UPDATE request
           SET ou_id = v_ou_id, div_id = v_div_id, grp_id = v_grp_id
         WHERE request_id = p_request_id;



        --get div/group code for calling API to get CIMSPRO users from server code
        IF v_has_chemical = 'Y'
        THEN
            SELECT g.org_cd
              INTO v_codes
              FROM request  r
                   INNER JOIN nist_user u ON r.requester_id = u.people_id
                   INNER JOIN nist_group g ON g.org_id = u.grp_org_id
             WHERE r.request_id = p_request_id;

            p_rc := 'Y,' || v_codes;
        END IF;
        
        IF v_has_taggable_equipment = 'Y'
        THEN
            p_pc_div:= v_pc_div;
        END IF;
        
    END IF;
EXCEPTION
    WHEN OTHERS
    THEN
        p_rc :=
               'Error ('
            || TO_CHAR (SQLCODE)
            || ') occurs from UP_CHECK_SUBMIT_REQUEST for request id: '
            || TO_CHAR (p_request_id)
            || '. '
            || SQLERRM;
END;
/
