
package com.weimob.jobserver.core.zk;

import lombok.Getter;
import lombok.Setter;

/**
 * 注册中心配置抽象类.
 * 
 * @author zhangliang
 */
@Getter
@Setter
public abstract class AbstractRegistryCenterConfiguration {
    
    /**
     * 本地属性文件路径.
     */
    private String localPropertiesPath;
    
    /**
     * 是否允许本地值覆盖注册中心.
     */
    private boolean overwrite;
}
