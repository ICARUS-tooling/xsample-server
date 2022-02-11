echo 'create-jdbc-connection-pool --datasourceclassname org.postgresql.ds.PGConnectionPoolDataSource --restype javax.sql.ConnectionPoolDataSource --property user=xsample:password=secret:DatabaseName=xsample:ServerName=${ENV=HOST_IP}:port=5432 XSamplePool' \
echo 'create-jdbc-resource --connectionpoolid XSamplePool jdbc/xsample' \
 >> $POSTBOOT_COMMANDS