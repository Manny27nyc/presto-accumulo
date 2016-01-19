-- $ID$
-- TPC-H/TPC-R National Market Share Query (Q8)
-- Functional Query Definition
-- Approved February 1998

SET SESSION accumulo.optimize_column_filters_enabled = false;
SET SESSION accumulo.optimize_range_predicate_pushdown_enabled = true;
SET SESSION accumulo.optimize_range_splits_enabled = true;
SET SESSION accumulo.secondary_index_enabled = true;

select
	o_year,
	sum(case
		when nation = 'BRAZIL' then volume
		else 0
	end) / sum(volume) as mkt_share
from
	(
		select
			extract(year from o.orderdate) as o_year,
			l.extendedprice * (1 - l.discount) as volume,
			n2.name as nation
		from
			${SCHEMA}.part p,
			${SCHEMA}.supplier s,
			${SCHEMA}.lineitem l,
			${SCHEMA}.orders o,
			${SCHEMA}.customer c,
			${SCHEMA}.nation n1,
			${SCHEMA}.nation n2,
			${SCHEMA}.region r
		where
			p.partkey = l.partkey
			and s.suppkey = l.suppkey
			and l.orderkey = o.orderkey
			and o.custkey = c.custkey
			and c.nationkey = n1.nationkey
			and n1.regionkey = r.regionkey
			and r.name = 'AMERICA'
			and s.nationkey = n2.nationkey
			and o.orderdate between date '1995-01-01' and date '1996-12-31'
			and p.type = 'ECONOMY ANODIZED STEEL'
	) as all_nations
group by
	o_year
order by
	o_year;
  
