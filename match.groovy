String string = "[My Table ]"
String string1 = "\"My Table \""
String string2 = "{My Table}"

println string.matches("[\\[*]My Table [\\]*]")
println string1.matches("\"[\\[]*My Table [\\]]*\"")
println string2.matches("[\\[]*My Table [\\]]*")
