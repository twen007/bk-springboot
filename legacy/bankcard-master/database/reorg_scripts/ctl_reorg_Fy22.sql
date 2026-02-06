/* Formatted on 4/27/2022 10:50:29 AM (QP5 v5.360) */
--ou_org_id, div_org_id, grp_org_id,requester_id

/*select r.grp_org_id as current_group_id, (select substr(org_cd,1,3) || '.' || substr(org_cd,4,2)  from nist_group where org_id=r.grp_org_id) as current_group,
 (select grp_org_id from nist_user where people_id=r.requester_id) as requesters_new_group_id, 
 (select substr(org_cd,1,3) || '.' || substr(org_cd,4,2) from nist_group where org_id=(select grp_org_id from nist_user where people_id=r.requester_id)) as requesters_new_group,
r.* from v_request r, nist_group g
where r.grp_org_id=g.org_id
and active_yn='N'
and r.fy=22;


select r.request_id
from request r, nist_group g
where r.grp_id=g.org_id
and active_yn='N'
and r.fy=22;

update request r
set r.GRP_ID= (select grp_org_id from nist_user where people_id=r.requester_id),
    r.div_id= (select div_org_id from nist_group where org_id=(select grp_org_id from nist_user where people_id=r.requester_id))
where request_id in (50181, 50150);

select * from request where request_id in (50181, 50150);

select * from nist_group
where org_id=14158;
*/

--After Reorg, check requests with inactive group
--then update the group to be the requester's new group,
--the division to be the requester's new division
UPDATE request r
   SET r.GRP_ID =
           (SELECT grp_org_id
              FROM nist_user
             WHERE people_id = r.requester_id),
       r.div_id =
           (SELECT div_org_id
              FROM nist_group
             WHERE org_id = (SELECT grp_org_id
                               FROM nist_user
                              WHERE people_id = r.requester_id)),
       r.updated_by = 23826,
       r.updated_date = SYSDATE
 WHERE request_id IN
           (SELECT r.request_id
              FROM request r, nist_group g
             WHERE r.grp_id = g.org_id AND active_yn = 'N' AND r.fy = 22);
             