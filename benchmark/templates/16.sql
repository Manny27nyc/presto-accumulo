-- $ID$
-- TPC-H/TPC-R Parts/Supplier Relationship Query (Q16)
-- Functional Query Definition
-- Approved February 1998

SET SESSION accumulo.optimize_column_filters_enabled = false;
SET SESSION accumulo.optimize_range_predicate_pushdown_enabled = true;
SET SESSION accumulo.optimize_range_splits_enabled = true;
SET SESSION accumulo.secondary_index_enabled = true;

select
	p.brand,
	p.type,
	p.size,
	count(distinct ps.suppkey) as supplier_cnt
from
	${SCHEMA}.partsupp ps,
	${SCHEMA}.part p
where
	p.partkey = ps.partkey
	and p.brand <> 'Brand#45'
	and p.type not like 'MEDIUM_POLISHED%'
  	and p.size in (49, 14, 23, 45, 19, 3, 36, 9)
	and ps.suppkey not in (
		select
			s.suppkey
		from
			${SCHEMA}.supplier s
		where
			s.comment like '%Customer%Complaints%'
	)
group by
	p.brand,
	p.type,
	p.size
order by
	supplier_cnt desc,
	p.brand,
	p.type,
	p.size;
