cd ../../../target
cd classes
del com\magic\example\Example1.class
cd com/magic/example
rename Example12.class Example1.class
cd ../../../
java com.magic.example.Example1
pause