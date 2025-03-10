package stirling.software.SPDF.utils;


import lombok.SneakyThrows;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

public class PluginImportBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar {
    private static final String TARGET_URL = "file:/stirling-pdf-enterprise-0.1.0.jar";

    private final String pluginClass;

    public PluginImportBeanDefinitionRegistrar(String pluginClass) {
        this.pluginClass = pluginClass;
    }

    @SneakyThrows
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        ClassLoader classLoader = ClassLoaderUtil.getClassLoader(TARGET_URL);
        Class<?> clazz = classLoader.loadClass(pluginClass);
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(clazz);
        BeanDefinition beanDefinition = builder.getBeanDefinition();

        registry.registerBeanDefinition(clazz.getName(), beanDefinition);
    }

}
