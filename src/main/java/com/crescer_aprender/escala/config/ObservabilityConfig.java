package com.crescer_aprender.escala.config;

import io.micrometer.core.instrument.binder.jvm.*;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ObservabilityConfig {

    @Bean public JvmMemoryMetrics jvmMemoryMetrics() { return new JvmMemoryMetrics(); }
    @Bean public JvmGcMetrics jvmGcMetrics()         { return new JvmGcMetrics(); }
    @Bean public JvmThreadMetrics jvmThreadMetrics() { return new JvmThreadMetrics(); }
    @Bean public ProcessorMetrics processorMetrics() { return new ProcessorMetrics(); }
}
