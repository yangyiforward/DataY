# TBDSReader 插件文档


___



## 1 快速介绍

TBDSReader插件实现了从TBDS读取数据。在底层实现上，TBDSReader通过JDBC连接远程TBDS，并执行相应的sql语句将数据从TBDS中SELECT出来。


## 3 功能说明

### 3.1 配置样例

* 配置一个从tbds数据库同步抽取数据到本地的作业:

```
{
    "job": {
        "setting": {
            "speed": {
                 "channel": 3
            },
            "errorLimit": {
                "record": 0,
                "percentage": 0.02
            }
        },
        "content": [
            {
                "reader": {
					"name": "tbdsreader",
					"parameter": {
						"haveDoneSql": true,
						"doneSql": "select * from test.ods_trd_orders_df limit 1",
						"connection": [
							{
								"jdbcUrl": ["jdbc:hive2://tbds-10-111-1-39:2181,tbds-10-111-1-46:2181,tbds-10-111-1-7:2181/;serviceDiscoveryMode=zooKeeper;zooKeeperNamespace=hiveserver2"],
								"querySql": ["select cust_code, cust_name from test.ods_trd_orders_df limit 100"],
							}
						],
						"username": "user",
						"password": "****",
					}
				},
               "writer": {
                    "name": "streamwriter",
                    "parameter": {
                        "print":true
                    }
                }
            }
        ]
    }
}

```


### 3.2 参数说明

* **jdbcUrl**

	* 描述：描述的是到对端数据库的JDBC连接信息，使用JSON的数组描述，并支持一个库填写多个连接地址。之所以使用JSON数组描述连接信息，是因为阿里集团内部支持多个IP探测，如果配置了多个，MysqlReader可以依次探测ip的可连接性，直到选择一个合法的IP。如果全部连接失败，MysqlReader报错。 注意，jdbcUrl必须包含在connection配置单元中。对于阿里集团外部使用情况，JSON数组填写一个JDBC连接即可。

		jdbcUrl按照Mysql官方规范，并可以填写连接附件控制信息。具体请参看[Mysql官方文档](http://dev.mysql.com/doc/connector-j/en/connector-j-reference-configuration-properties.html)。

	* 必选：是 <br />

	* 默认值：无 <br />

* **username**

	* 描述：数据源的用户名 <br />

	* 必选：是 <br />

	* 默认值：无 <br />

* **password**

	* 描述：数据源指定用户名的密码 <br />

	* 必选：是 <br />

	* 默认值：无 <br />

* **haveDoneSql**

	* 描述：是否需要标志SQL校验 <br />

	* 必选：否 <br />

	* 默认值：否 <br />

* **doneSql**

	* 描述：标志SQL，如标志SQL返回记录数大于等于1条，则读取数据；否则报错 <br />

	* 必选：当haveDoneSql为true时为是 <br />

	* 默认值：无 <br />

* **table**

	* 描述：所选取的需要同步的表。使用JSON的数组描述，因此支持多张表同时抽取。当配置为多张表时，用户自己需保证多张表是同一schema结构，MysqlReader不予检查表是否同一逻辑表。注意，table必须包含在connection配置单元中。<br />

	* 必选：是 <br />

	* 默认值：无 <br />

* **column**

	* 描述：所配置的表中需要同步的列名集合，使用JSON的数组描述字段信息。用户使用\*代表默认使用所有列配置，例如['\*']。

	  支持列裁剪，即列可以挑选部分列进行导出。

      支持列换序，即列可以不按照表schema信息进行导出。

	  支持常量配置，用户需要按照Mysql SQL语法格式:
	  ["id", "\`table\`", "1", "'bazhen.csy'", "null", "to_char(a + 1)", "2.3" , "true"]
	  id为普通列名，\`table\`为包含保留在的列名，1为整形数字常量，'bazhen.csy'为字符串常量，null为空指针，to_char(a + 1)为表达式，2.3为浮点数，true为布尔值。

	* 必选：是 <br />

	* 默认值：无 <br />

* **where**

	* 描述：筛选条件，MysqlReader根据指定的column、table、where条件拼接SQL，并根据这个SQL进行数据抽取。在实际业务场景中，往往会选择当天的数据进行同步，可以将where条件指定为gmt_create > $bizdate 。注意：不可以将where条件指定为limit 10，limit不是SQL的合法where子句。<br />

          where条件可以有效地进行业务增量同步。如果不填写where语句，包括不提供where的key或者value，DataX均视作同步全量数据。

	* 必选：否 <br />

	* 默认值：无 <br />

* **querySql**

	* 描述：在有些业务场景下，where这一配置项不足以描述所筛选的条件，用户可以通过该配置型来自定义筛选SQL。当用户配置了这一项之后，DataX系统就会忽略table，column这些配置型，直接使用这个配置项的内容对数据进行筛选，例如需要进行多表join后同步数据，使用select a,b from table_a join table_b on table_a.id = table_b.id <br />

	 `当用户配置querySql时，MysqlReader直接忽略table、column、where条件的配置`，querySql优先级大于table、column、where选项。

	* 必选：否 <br />

	* 默认值：无 <br />
	


