package com.sandbox.home.enumeration;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * 数据源枚举
 * <p>
 * 定义系统所有数据源标识：DS0/DS1（主库）、DS0SLAVE0/DS1SLAVE0（从库）。
 * 用于 ShardingSphere 读写分离配置，避免硬编码数据源名称。
 *
 * @author 0101
 * @since 2026-03-13
 */
@Getter
public enum DataSourceEnum {

    DS0("ds0", "数据源0"),
    DS1("ds1", "数据源1"),
    DS0SLAVE0("ds0slave0", "数据源0的备用0"),
    DS1SLAVE0("ds1slave0", "数据源1的备用0");

    /**
     * 数据源标识值
     */
    private final String value;

    /**
     * 数据源描述
     */
    private final String description;

    DataSourceEnum(String value, String description) {
        this.value = value;
        this.description = description;
    }

    /**
     * 根据标识值获取描述，找不到抛出 NoSuchElementException
     */
    public static String getDescriptionByValue(String value) {
        return Arrays.stream(values())
                .filter(x -> x.getValue().equals(value))
                .findFirst()
                .map(DataSourceEnum::getDescription)
                .orElseThrow(() -> new NoSuchElementException("没有找到对应的枚举！"));
    }

    /**
     * 获取所有枚举列表
     */
    public static List<DataSourceEnum> getList() {
        return Arrays.stream(DataSourceEnum.values()).collect(Collectors.toList());
    }
}