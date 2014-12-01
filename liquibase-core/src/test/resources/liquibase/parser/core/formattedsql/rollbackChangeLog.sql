--liquibase formatted sql logicalFilePath:test-path

--changeset test-author:multi-line
--comment: Add new column to the table: step 1
ALTER TABLE grant_reference.program_type ADD COLUMN program_type_abbr text;

--rollback
ALTER TABLE grant_reference.program_type
	DROP COLUMN program_type_abbr;
DROP table xyz dual;

--changeset test-author:empty-rollback runInTransaction:false
--comment: Insert data into the new column: step 2
update dual set 1=2;

--rollback    

--changeset test-author:multi-rollback
select * from dual;
--rollback drop table dual;
--rollback drop table d2;
drop table d3;
