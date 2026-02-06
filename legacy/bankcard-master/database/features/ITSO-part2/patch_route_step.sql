--check for requests still in the approval process that has dynamic routing
--the patch logic doesn't work for these requests. some manual updates needed
select * from v_request_current_route
where route_status_id not in (8,9,10,13)
and (is_dynamic =1 or is_dynamic_reroute = 1 or reroute_stack >0);


--patch existing data in the route table with route steps based on route_id for each request
declare 
 step integer := 1;
 cur_req integer :=0;
begin
--order all records by request_id and route_id first
for rec in (select * from route order by request_id,route_id) --where request_id in (3762,3763,3781) 
--loop all records
loop
    --for init, first record, set current request and step number
    if cur_req = 0 then 
        cur_req := rec.request_id; 
        step := 1;  
    else 
        --for subsequent records, if it's still the same request, increase the step number by 1
        if (cur_req = rec.request_id) then
            step:=step+1;
        --if it's a different request, change the current request to that request and reset the step number to 1
        else
           cur_req := rec.request_id;
           step:=1;
        end if;
    end if; 
    --update the step number for a record in the route table with the same request and route_id as the record in the loop
    update route set route_step=step where request_id=rec.request_id and route_id=rec.route_id;
end loop;
end;



--patch request table with current_route values
update request a
set a.current_route = (select route_id from route where request_id =a.request_id
and route_step = (select max(route_step) from route where request_id =a.request_id));