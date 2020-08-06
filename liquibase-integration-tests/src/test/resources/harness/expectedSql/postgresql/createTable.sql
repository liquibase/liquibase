CREATE TABLE public.test_table (test_id INTEGER NOT NULL, test_column VARCHAR(50) NOT NULL, CONSTRAINT TEST_TABLE_PKEY PRIMARY KEY (test_id))
CREATE VIEW public.test_view AS select * from test_table