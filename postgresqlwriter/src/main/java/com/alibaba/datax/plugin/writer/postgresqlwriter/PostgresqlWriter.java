package com.alibaba.datax.plugin.writer.postgresqlwriter;

import com.alibaba.datax.common.exception.DataXException;
import com.alibaba.datax.common.plugin.RecordReceiver;
import com.alibaba.datax.common.spi.Writer;
import com.alibaba.datax.common.util.Configuration;
import com.alibaba.datax.plugin.rdbms.util.DBUtilErrorCode;
import com.alibaba.datax.plugin.rdbms.util.DataBaseType;
import com.alibaba.datax.plugin.rdbms.writer.CommonRdbmsWriter;
import com.alibaba.datax.plugin.rdbms.writer.Key;

import java.util.List;

public class PostgresqlWriter extends Writer {
	private static final DataBaseType DATABASE_TYPE = DataBaseType.PostgreSQL;

	public static class Job extends Writer.Job {
		private Configuration originalConfig = null;
		private CommonRdbmsWriter.Job commonRdbmsWriterMaster;

		@Override
		public void init() {
			this.originalConfig = super.getPluginJobConf();

			// modify by xiezhentao on 2022-04-19, add update support to postgresql
			String writeMode = this.originalConfig.getString(Key.WRITE_MODE);
			if (null != writeMode) {
				if (!"insert".equalsIgnoreCase(writeMode) && !writeMode.startsWith("update")) {
					throw DataXException.asDataXException(DBUtilErrorCode.CONF_ERROR,
							String.format("写入模式(writeMode)配置错误. PostgreSQL 仅支持insert, update两种模式. %s 不支持", writeMode));
				}
			}

			this.commonRdbmsWriterMaster = new CommonRdbmsWriter.Job(DATABASE_TYPE);
			this.commonRdbmsWriterMaster.init(this.originalConfig);
		}

		@Override
		public void prepare() {
			this.commonRdbmsWriterMaster.prepare(this.originalConfig);
		}

		@Override
		public List<Configuration> split(int mandatoryNumber) {
			return this.commonRdbmsWriterMaster.split(this.originalConfig, mandatoryNumber);
		}

		@Override
		public void post() {
			this.commonRdbmsWriterMaster.post(this.originalConfig);
		}

		@Override
		public void destroy() {
			this.commonRdbmsWriterMaster.destroy(this.originalConfig);
		}

	}

	public static class Task extends Writer.Task {
		private Configuration writerSliceConfig;
		private CommonRdbmsWriter.Task commonRdbmsWriterSlave;

		@Override
		public void init() {
			this.writerSliceConfig = super.getPluginJobConf();
			this.commonRdbmsWriterSlave = new CommonRdbmsWriter.Task(DATABASE_TYPE){
				@Override
				public String calcValueHolder(String columnType){
					if("serial".equalsIgnoreCase(columnType)){
						return "?::int";
					}else if("bigserial".equalsIgnoreCase(columnType)){
						return "?::int8";
					}else if("bit".equalsIgnoreCase(columnType)){
						return "?::bit varying";
					}
					return "?::" + columnType;
				}
			};
			this.commonRdbmsWriterSlave.init(this.writerSliceConfig);
		}

		@Override
		public void prepare() {
			this.commonRdbmsWriterSlave.prepare(this.writerSliceConfig);
		}

		@Override
        public void startWrite(RecordReceiver recordReceiver) {
			this.commonRdbmsWriterSlave.startWrite(recordReceiver, this.writerSliceConfig, super.getTaskPluginCollector());
		}

		@Override
		public void post() {
			this.commonRdbmsWriterSlave.post(this.writerSliceConfig);
		}

		@Override
		public void destroy() {
			this.commonRdbmsWriterSlave.destroy(this.writerSliceConfig);
		}

	}

}
