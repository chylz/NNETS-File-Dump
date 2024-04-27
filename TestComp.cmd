java BMP2OneByte t1.bmp t1.bin
java ToIntArray 4032 3024 t1.bin
java BGR2BMP bw 100 100 modifiedt1.bin processed_t1.bmp
java BMP2OneByte t2.bmp t2.bin
java ToIntArray 4032 3024 t2.bin
java BGR2BMP bw 100 100 modifiedt2.bin processed_t2.bmp
java BMP2OneByte t3.bmp t3.bin
java ToIntArray 4032 3024 t3.bin
java BGR2BMP bw 100 100 modifiedt3.bin processed_t3.bmp
java BMP2OneByte t4.bmp t4.bin
java ToIntArray 4032 3024 t4.bin
java BGR2BMP bw 100 100 modifiedt4.bin processed_t4.bmp
java BMP2OneByte t5.bmp t5.bin
java ToIntArray 4032 3024 t5.bin
java BGR2BMP bw 100 100 modifiedt5.bin processed_t5.bmp
java -Xmx5g Aggregate modifiedt1.bin modifiedt2.bin modifiedt3.bin modifiedt4.bin modifiedt5.bin 