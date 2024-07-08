--liquibase formatted sql

--changeset example:0
CREATE TABLE Gender (
                        Id INT PRIMARY KEY,
                        Gender TEXT NOT NULL
);

--changeset example:1 runOnChange:true
MERGE INTO Gender AS t
    USING (
        VALUES
            (1, 'Male'),
            (2, 'Female'),
            (3, 'Non-Binary'),
            (4, 'Unknown')
    ) AS s (id, Gender)
    ON t.id = s.id
    WHEN MATCHED THEN
        UPDATE SET t.Gender = s.Gender
    WHEN NOT MATCHED THEN
        INSERT (id, Gender) VALUES (s.id, s.Gender);
