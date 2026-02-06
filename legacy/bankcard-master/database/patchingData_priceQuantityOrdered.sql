--patch data caused by item's save and add function which cause the actual price and quantity be different
--than the price and qunatity in prepared, saved, submitted, reviewed, approved stage.
--Note: after approved stage, BHs can change actual price and quantity
create table tmp_patch_price_quantity as
select a.route_type_name,b.request_id,b.item_id,b.price,b.price_ordered,b.quantity,b.quantity_ordered from v_request a, item b
where A.TOTAL_COST!=A.ACTUAL_TOTAL_COST
and A.ACTUAL_TOTAL_COST<>0
and a.route_type_id in (0,1,2,3,12)
and a.request_id=b.request_id
and (b.price<>b.price_ordered or b.quantity<>b.quantity_ordered);

update item i
set i.price_ordered = i.price
where i.item_id in (select a.item_id from tmp_patch_price_quantity a
where a.price<>a.price_ordered);

update item i
set i.quantity_ordered = i.quantity
where i.item_id in (select a.item_id from tmp_patch_price_quantity a
where a.quantity<>a.quantity_ordered);

drop table tmp_patch_price_quantity;