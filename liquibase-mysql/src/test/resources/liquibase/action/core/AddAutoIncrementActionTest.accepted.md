**NOTE: This output is generated and parsed by TestMD. Please read it, but DO NOT EDIT MANUALLY**

# Test: "Can apply standard settings to complex names" #

- **connection:** mysql[config:caseInsensitive]

| Permutation | Verified | columnName                                                       | OPERATIONS
| :---------- | :------- | :--------------------------------------------------------------- | :------
| f716ec      | true     | LBSCHEMA.4test_table.4test_column                                | **plan**: ALTER TABLE `LBSCHEMA`.`4test_table` MODIFY `4test_column` int AUTO_INCREMENT
| c2b510      | true     | LBSCHEMA.4test_table.anotherlowercolumn                          | **plan**: ALTER TABLE `LBSCHEMA`.`4test_table` MODIFY `anotherlowercolumn` int AUTO_INCREMENT
| ab8646      | true     | LBSCHEMA.4test_table.crazy!@#$%^&*()_+{}[]column                 | **plan**: ALTER TABLE `LBSCHEMA`.`4test_table` MODIFY `crazy!@#$%^&*()_+{}[]column` int AUTO_INCREMENT
| 34c444      | true     | LBSCHEMA.4test_table.lowercolumn                                 | **plan**: ALTER TABLE `LBSCHEMA`.`4test_table` MODIFY `lowercolumn` int AUTO_INCREMENT
| 1637ef      | true     | LBSCHEMA.4test_table.only_in_null                                | **plan**: ALTER TABLE `LBSCHEMA`.`4test_table` MODIFY `only_in_null` int AUTO_INCREMENT
| 7f1854      | true     | LBSCHEMA.anotherlowertable.4test_column                          | **plan**: ALTER TABLE `LBSCHEMA`.`anotherlowertable` MODIFY `4test_column` int AUTO_INCREMENT
| 4823fa      | true     | LBSCHEMA.anotherlowertable.anotherlowercolumn                    | **plan**: ALTER TABLE `LBSCHEMA`.`anotherlowertable` MODIFY `anotherlowercolumn` int AUTO_INCREMENT
| 191c7a      | true     | LBSCHEMA.anotherlowertable.crazy!@#$%^&*()_+{}[]column           | **plan**: ALTER TABLE `LBSCHEMA`.`anotherlowertable` MODIFY `crazy!@#$%^&*()_+{}[]column` int AUTO_INCREMENT
| 416dbb      | true     | LBSCHEMA.anotherlowertable.lowercolumn                           | **plan**: ALTER TABLE `LBSCHEMA`.`anotherlowertable` MODIFY `lowercolumn` int AUTO_INCREMENT
| 400934      | true     | LBSCHEMA.anotherlowertable.only_in_null                          | **plan**: ALTER TABLE `LBSCHEMA`.`anotherlowertable` MODIFY `only_in_null` int AUTO_INCREMENT
| c5760a      | true     | LBSCHEMA.crazy!@#$%^&*()_+{}[]table.4test_column                 | **plan**: ALTER TABLE `LBSCHEMA`.`crazy!@#$%^&*()_+{}[]table` MODIFY `4test_column` int AUTO_INCREMENT
| 80fb0a      | true     | LBSCHEMA.crazy!@#$%^&*()_+{}[]table.anotherlowercolumn           | **plan**: ALTER TABLE `LBSCHEMA`.`crazy!@#$%^&*()_+{}[]table` MODIFY `anotherlowercolumn` int AUTO_INCREMENT
| 75f9be      | true     | LBSCHEMA.crazy!@#$%^&*()_+{}[]table.crazy!@#$%^&*()_+{}[]column  | **plan**: ALTER TABLE `LBSCHEMA`.`crazy!@#$%^&*()_+{}[]table` MODIFY `crazy!@#$%^&*()_+{}[]column` int AUTO_INCREMENT
| 1495f8      | true     | LBSCHEMA.crazy!@#$%^&*()_+{}[]table.lowercolumn                  | **plan**: ALTER TABLE `LBSCHEMA`.`crazy!@#$%^&*()_+{}[]table` MODIFY `lowercolumn` int AUTO_INCREMENT
| 4271e9      | true     | LBSCHEMA.crazy!@#$%^&*()_+{}[]table.only_in_null                 | **plan**: ALTER TABLE `LBSCHEMA`.`crazy!@#$%^&*()_+{}[]table` MODIFY `only_in_null` int AUTO_INCREMENT
| 846372      | true     | LBSCHEMA.lowertable.4test_column                                 | **plan**: ALTER TABLE `LBSCHEMA`.`lowertable` MODIFY `4test_column` int AUTO_INCREMENT
| f08518      | true     | LBSCHEMA.lowertable.anotherlowercolumn                           | **plan**: ALTER TABLE `LBSCHEMA`.`lowertable` MODIFY `anotherlowercolumn` int AUTO_INCREMENT
| 1fd760      | true     | LBSCHEMA.lowertable.crazy!@#$%^&*()_+{}[]column                  | **plan**: ALTER TABLE `LBSCHEMA`.`lowertable` MODIFY `crazy!@#$%^&*()_+{}[]column` int AUTO_INCREMENT
| 8d5606      | true     | LBSCHEMA.lowertable.lowercolumn                                  | **plan**: ALTER TABLE `LBSCHEMA`.`lowertable` MODIFY `lowercolumn` int AUTO_INCREMENT
| 16f864      | true     | LBSCHEMA.lowertable.only_in_null                                 | **plan**: ALTER TABLE `LBSCHEMA`.`lowertable` MODIFY `only_in_null` int AUTO_INCREMENT
| e5af77      | true     | LBSCHEMA2.4test_table.4test_column                               | **plan**: ALTER TABLE `LBSCHEMA2`.`4test_table` MODIFY `4test_column` int AUTO_INCREMENT
| 22cd9e      | true     | LBSCHEMA2.4test_table.anotherlowercolumn                         | **plan**: ALTER TABLE `LBSCHEMA2`.`4test_table` MODIFY `anotherlowercolumn` int AUTO_INCREMENT
| 004e8a      | true     | LBSCHEMA2.4test_table.crazy!@#$%^&*()_+{}[]column                | **plan**: ALTER TABLE `LBSCHEMA2`.`4test_table` MODIFY `crazy!@#$%^&*()_+{}[]column` int AUTO_INCREMENT
| 83d61a      | true     | LBSCHEMA2.4test_table.lowercolumn                                | **plan**: ALTER TABLE `LBSCHEMA2`.`4test_table` MODIFY `lowercolumn` int AUTO_INCREMENT
| e627b0      | true     | LBSCHEMA2.4test_table.only_in_null                               | **plan**: ALTER TABLE `LBSCHEMA2`.`4test_table` MODIFY `only_in_null` int AUTO_INCREMENT
| ce11a7      | true     | LBSCHEMA2.anotherlowertable.4test_column                         | **plan**: ALTER TABLE `LBSCHEMA2`.`anotherlowertable` MODIFY `4test_column` int AUTO_INCREMENT
| 310dfb      | true     | LBSCHEMA2.anotherlowertable.anotherlowercolumn                   | **plan**: ALTER TABLE `LBSCHEMA2`.`anotherlowertable` MODIFY `anotherlowercolumn` int AUTO_INCREMENT
| 414253      | true     | LBSCHEMA2.anotherlowertable.crazy!@#$%^&*()_+{}[]column          | **plan**: ALTER TABLE `LBSCHEMA2`.`anotherlowertable` MODIFY `crazy!@#$%^&*()_+{}[]column` int AUTO_INCREMENT
| 9e6805      | true     | LBSCHEMA2.anotherlowertable.lowercolumn                          | **plan**: ALTER TABLE `LBSCHEMA2`.`anotherlowertable` MODIFY `lowercolumn` int AUTO_INCREMENT
| d35e51      | true     | LBSCHEMA2.anotherlowertable.only_in_null                         | **plan**: ALTER TABLE `LBSCHEMA2`.`anotherlowertable` MODIFY `only_in_null` int AUTO_INCREMENT
| 546deb      | true     | LBSCHEMA2.crazy!@#$%^&*()_+{}[]table.4test_column                | **plan**: ALTER TABLE `LBSCHEMA2`.`crazy!@#$%^&*()_+{}[]table` MODIFY `4test_column` int AUTO_INCREMENT
| 1bcf55      | true     | LBSCHEMA2.crazy!@#$%^&*()_+{}[]table.anotherlowercolumn          | **plan**: ALTER TABLE `LBSCHEMA2`.`crazy!@#$%^&*()_+{}[]table` MODIFY `anotherlowercolumn` int AUTO_INCREMENT
| ac50ae      | true     | LBSCHEMA2.crazy!@#$%^&*()_+{}[]table.crazy!@#$%^&*()_+{}[]column | **plan**: ALTER TABLE `LBSCHEMA2`.`crazy!@#$%^&*()_+{}[]table` MODIFY `crazy!@#$%^&*()_+{}[]column` int AUTO_INCREMENT
| 1668b1      | true     | LBSCHEMA2.crazy!@#$%^&*()_+{}[]table.lowercolumn                 | **plan**: ALTER TABLE `LBSCHEMA2`.`crazy!@#$%^&*()_+{}[]table` MODIFY `lowercolumn` int AUTO_INCREMENT
| 432ba7      | true     | LBSCHEMA2.crazy!@#$%^&*()_+{}[]table.only_in_null                | **plan**: ALTER TABLE `LBSCHEMA2`.`crazy!@#$%^&*()_+{}[]table` MODIFY `only_in_null` int AUTO_INCREMENT
| a741aa      | true     | LBSCHEMA2.lowertable.4test_column                                | **plan**: ALTER TABLE `LBSCHEMA2`.`lowertable` MODIFY `4test_column` int AUTO_INCREMENT
| 815c5b      | true     | LBSCHEMA2.lowertable.anotherlowercolumn                          | **plan**: ALTER TABLE `LBSCHEMA2`.`lowertable` MODIFY `anotherlowercolumn` int AUTO_INCREMENT
| 6ae818      | true     | LBSCHEMA2.lowertable.crazy!@#$%^&*()_+{}[]column                 | **plan**: ALTER TABLE `LBSCHEMA2`.`lowertable` MODIFY `crazy!@#$%^&*()_+{}[]column` int AUTO_INCREMENT
| 502666      | true     | LBSCHEMA2.lowertable.lowercolumn                                 | **plan**: ALTER TABLE `LBSCHEMA2`.`lowertable` MODIFY `lowercolumn` int AUTO_INCREMENT
| a6b2d6      | true     | LBSCHEMA2.lowertable.only_in_null                                | **plan**: ALTER TABLE `LBSCHEMA2`.`lowertable` MODIFY `only_in_null` int AUTO_INCREMENT

# Test: "Valid parameter permutations work" #

- **connection:** mysql[config:caseInsensitive]

| Permutation | Verified | columnName               | startWith | OPERATIONS
| :---------- | :------- | :----------------------- | :-------- | :------
| ddb610      | true     | LBSCHEMA2.table1.column2 |           | **plan**: ALTER TABLE `LBSCHEMA2`.`table1` MODIFY `column2` int AUTO_INCREMENT
| 8a9886      | true     | LBSCHEMA2.table1.column2 | 1         | **plan**: ALTER TABLE `LBSCHEMA2`.`table1` MODIFY `column2` int AUTO_INCREMENT<br>AND THEN: ALTER TABLE `LBSCHEMA2`.`table1` AUTO_INCREMENT=1
| 4ee168      | true     | LBSCHEMA2.table1.column2 | 10        | **plan**: ALTER TABLE `LBSCHEMA2`.`table1` MODIFY `column2` int AUTO_INCREMENT<br>AND THEN: ALTER TABLE `LBSCHEMA2`.`table1` AUTO_INCREMENT=10
| 7fa658      | true     | LBSCHEMA2.table1.column2 | 2         | **plan**: ALTER TABLE `LBSCHEMA2`.`table1` MODIFY `column2` int AUTO_INCREMENT<br>AND THEN: ALTER TABLE `LBSCHEMA2`.`table1` AUTO_INCREMENT=2
