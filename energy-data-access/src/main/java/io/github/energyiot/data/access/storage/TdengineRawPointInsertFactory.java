package io.github.energyiot.data.access.storage;

public class TdengineRawPointInsertFactory implements RawPointInsertFactory {

    @Override
    public String createInsertSql(String tableName) {
        return "INSERT INTO `" + tableName + "` " +
                "(id,message_id,protocol_version,tenant_mark,model_mark,device_mark,param_mark,raw_value," +
                "device_time,receive_time,normal_second,created_time) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
    }
}
