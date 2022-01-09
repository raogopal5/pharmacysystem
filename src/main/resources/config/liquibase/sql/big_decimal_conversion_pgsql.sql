alter table stock add cost_new varchar;

alter table stock add mrp_new varchar;

alter table stock add stock_value_new varchar;

alter table stock add original_mrp_new varchar;

update stock set cost_new = cost, mrp_new = mrp, stock_value_new = stock_value, original_mrp_new = original_mrp;

alter table stock alter column cost type numeric(18,6) using cast(cost_new as numeric(18,6));

alter table stock alter column mrp type numeric(18,6) using cast(mrp_new as numeric(18,6));

alter table stock alter column stock_value type numeric(18,6) using cast(stock_value_new as numeric(18,6));

alter table stock alter column original_mrp type numeric(18,6) using cast(original_mrp_new as numeric(18,6));

alter table stock_source add cost_new varchar;

alter table stock_source add mrp_new varchar;

alter table stock_source add original_mrp_new varchar;

update stock_source set cost_new = cost, mrp_new = mrp, original_mrp_new = original_mrp;

alter table stock_source alter column cost type numeric(18,6) using cast(cost_new as numeric(18,6));

alter table stock_source alter column mrp type numeric(18,6) using cast(mrp_new as numeric(18,6));

alter table stock_source alter column original_mrp type numeric(18,6) using cast(original_mrp_new as numeric(18,6));

alter table stock_flow add cost_new varchar;

alter table stock_flow add mrp_new varchar;

alter table stock_flow add average_cost_new varchar;

alter table stock_flow add cost_value_new varchar;

alter table stock_flow add average_cost_value_new varchar;

update stock_flow set cost_new = cost, mrp_new = mrp, average_cost_new = average_cost, cost_value_new = cost_value, average_cost_value_new = average_cost_value;

alter table stock_flow alter column cost type numeric(18,6) using cast(cost_new as numeric(18,6));

alter table stock_flow alter column mrp type numeric(18,6) using cast(mrp_new as numeric(18,6));

alter table stock_flow alter column average_cost type numeric(18,6) using cast(average_cost_new as numeric(18,6));

alter table stock_flow alter column cost_value type numeric(18,6) using cast(cost_value_new as numeric(18,6));

alter table stock_flow alter column average_cost_value type numeric(18,6) using cast(average_cost_value_new as numeric(18,6));

alter table item_unit_average_cost add average_cost_new varchar;

update item_unit_average_cost set average_cost_new = average_cost;

alter table item_unit_average_cost alter column average_cost type numeric(18,6) using cast(average_cost_new as numeric(18,6));
