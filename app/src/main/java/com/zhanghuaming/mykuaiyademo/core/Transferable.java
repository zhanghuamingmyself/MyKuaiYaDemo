package com.zhanghuaming.mykuaiyademo.core;

/**
 * Created by zhanghuaming on 2016/11/10.
 * Contact me zhanghuamingmyself@163.com
 */
public interface Transferable {



    /**
     *
     * @throws Exception
     */
    void init() throws Exception;


    /**
     *
     * @throws Exception
     */
    void parseHeader() throws Exception;


    /**
     *
     * @throws Exception
     */
    void parseBody() throws Exception;


    /**
     *
     * @throws Exception
     */
    void finish() throws Exception;
}
