package com.frontend.ISAPI;

import com.serotonin.modbus4j.BatchRead;
import com.serotonin.modbus4j.BatchResults;
import com.serotonin.modbus4j.ModbusMaster;
import com.serotonin.modbus4j.code.DataType;
import com.serotonin.modbus4j.exception.ErrorResponseException;
import com.serotonin.modbus4j.exception.ModbusTransportException;
import com.serotonin.modbus4j.locator.BaseLocator;

public class Modbus4jReadUtil {

    /**
     * 读取[01 Coil Status 0x]类型 开关数据
     *
     * @param slaveId slaveId
     * @param offset  位置
     * @return 读取值
     * @throws ModbusTransportException 异常
     * @throws ErrorResponseException   异常
     */
    public static Boolean readCoilStatus(ModbusMaster master, int slaveId, int offset, String dev_code){
        // 01 Coil Status
        BaseLocator<Boolean> loc = BaseLocator.coilStatus(slaveId, offset);
        try {
            return master.getValue(loc);
        }catch (Exception e){
            if (e.getMessage().equals("java.net.SocketTimeoutException: connect timed out")) System.err.println(dev_code+"："+e.getMessage());
            else e.printStackTrace();
            return null;
        }
    }


    /**
     * 读取[02 Input Status 1x]类型 开关数据
     *
     * @param slaveId
     * @param offset
     * @return
     * @throws ModbusTransportException
     * @throws ErrorResponseException
     */
    public static Boolean readInputStatus(ModbusMaster master,int slaveId, int offset,String dev_code) {
        // 02 Input Status
        BaseLocator<Boolean> loc = BaseLocator.inputStatus(slaveId, offset);
        try{
            return master.getValue(loc);
        }catch (Exception e){
            if (e.getMessage().equals("java.net.SocketTimeoutException: connect timed out")) System.err.println(dev_code+"："+e.getMessage());
            else e.printStackTrace();
            return null;
        }
    }


    /**
     * 读取[03 Holding Register类型 2x]模拟量数据
     *
     * @param slaveId  slave Id
     * @param offset   位置
     * @param dataType 数据类型,来自com.serotonin.modbus4j.code.DataType
     * @return
     * @throws ModbusTransportException 异常
     * @throws ErrorResponseException   异常
     */
    public static Number readHoldingRegister(ModbusMaster master,int slaveId, int offset, int dataType,String dev_code) {
        // 03 Holding Register类型数据读取
        BaseLocator<Number> loc = BaseLocator.holdingRegister(slaveId, offset, dataType);
        try {
            return master.getValue(loc);
        }catch (Exception e){
            if (e.getMessage().equals("java.net.SocketTimeoutException: connect timed out")) System.err.println(dev_code+"："+e.getMessage());
            else e.printStackTrace();
            return null;
        }
    }


    /**
     * 读取[04 Input Registers 3x]类型 模拟量数据
     *
     * @param slaveId  slaveId
     * @param offset   位置
     * @param dataType 数据类型,来自com.serotonin.modbus4j.code.DataType
     * @return 返回结果
     * @throws ModbusTransportException 异常
     * @throws ErrorResponseException   异常
     */
    public static Number readInputRegisters(ModbusMaster master,int slaveId, int offset, int dataType,String dev_code) {
        // 04 Input Registers类型数据读取
        BaseLocator<Number> loc = BaseLocator.inputRegister(slaveId, offset, dataType);
        try{
            return master.getValue(loc);
        }catch (Exception e){
            if (e.getMessage().equals("java.net.SocketTimeoutException: connect timed out")) System.err.println(dev_code+"："+e.getMessage());
            else e.printStackTrace();
            return null;
        }
    }

    /**
     * 批量读取使用方法
     *
     * @throws ModbusTransportException
     * @throws ErrorResponseException
     */
    public static void batchRead(ModbusMaster master) throws ModbusTransportException, ErrorResponseException {
        BatchRead<Integer> batch = new BatchRead<Integer>();
        batch.addLocator(0, BaseLocator.holdingRegister(1, 1, DataType.TWO_BYTE_INT_SIGNED));
        batch.addLocator(1, BaseLocator.inputStatus(1, 0));
        batch.setContiguousRequests(true);
        BatchResults<Integer> results = master.send(batch);
        System.out.println("batchRead:" + results.getValue(0));
        System.out.println("batchRead:" + results.getValue(1));
    }

}