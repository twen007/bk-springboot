insert into role_type (role_id, role_name)
select 1, 'Admin' from dual
union
select 2, 'Bankcard Approving Official' from dual
union
select 3, 'Bankcard Holder' from dual;

INSERT INTO cpr_people
           (PEOPLE_ID
           ,FIRST_NAME
           ,LAST_NAME
           ,MID_NAME
           ,OU_ORG_ID
           ,DIV_ORG_ID
           ,USERNAME
           ,EMAIL
           ,PHONE
           ,SUPERVISOR_YN
           ,LAST_UPDATE_DT
           ,GRP_ORG_ID)
select 23826,'Xinwei','Wen','NMN',13204,13225,'xinweiw','xinwei.wen@nist.gov','3019755509','N', to_date('01-Feb-2017', 'dd-mon-yyyy'),13316 from dual union
select 27170,'Youchun','Yao','NMN',13204,13225,'youchun','youchun.yao@nist.gov','3019754955','N', to_date('01-Feb-2017', 'dd-mon-yyyy'),13316 from dual union
select 208157,'Jason','Poffenberger','T',13204,13225,'jtp1','jason.poffenberger@nist.gov','3019754053','N', to_date('01-Feb-2017', 'dd-mon-yyyy'),13316 from dual;

insert into user_role (people_id, role_id)
select 23826, 1 from dual
union
select 23826, 2 from dual
union
select 23826, 3 from dual
union
select 208157, 1 from dual
union
select 208157, 2 from dual
union
select 208157, 3 from dual
union
select 27170, 1 from dual
union
select 27170, 2 from dual
union
select 27170, 3 from dual;