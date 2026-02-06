--create a temp table to store approved amount for requests that passed the approval step
create table tmp_approval_amount
as
select a.request_id, get_request_total_cost(a.request_id) as approval_amount
from v_request a
where a.route_type_id in (3, 4,6,7); --9 reroute

--update the approval_amount in the request table using the temp table
update request r
set R.APPROVAL_AMOUNT=(select A.APPROVAL_AMOUNT from tmp_approval_amount a where r.request_id=a.request_id);

--patching approval amount finished. remove the temp table
drop table tmp_approval_amount;


