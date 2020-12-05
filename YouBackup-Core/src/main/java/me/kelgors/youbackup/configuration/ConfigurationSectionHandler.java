package me.kelgors.youbackup.configuration;

import org.bukkit.configuration.ConfigurationSection;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;

public class ConfigurationSectionHandler implements InvocationHandler {

    private final ConfigurationSection mSection;
    private final List<String> BLOCK_LIST = Arrays.asList("getRoot", "getParent", "set", "createSection", "getDefaultSection", "addDefault");

    public ConfigurationSectionHandler(ConfigurationSection section) {
        mSection = section;
    }

    @Override
    public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
        if (BLOCK_LIST.contains(method.getName())) {
            throw new IllegalAccessException(String.format("ReadOnly section, unable to call %s", method.getName()));
        }
        final Object result = method.invoke(mSection, objects);
        if ("getConfigurationSection".equals(method.getName())) {
            return Proxy.newProxyInstance(
                ConfigurationSection.class.getClassLoader(),
                new Class[] { ConfigurationSection.class },
                new ConfigurationSectionHandler((ConfigurationSection) result)
            );
        }
        return result;
    }
}
