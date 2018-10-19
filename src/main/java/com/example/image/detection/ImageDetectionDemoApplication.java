package com.example.image.detection;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;

import com.example.image.detection.property.FileStorageProperties;

@SpringBootApplication
@EnableRabbit

@EnableConfigurationProperties({
		FileStorageProperties.class
})
public class ImageDetectionDemoApplication implements RabbitListenerConfigurer {
	
	public static final String EXCHANGE_NAME = "appExchange";
	public static final String QUEUE_IN_NAME = "inboundImageQueue";
	public static final String QUEUE_OUT_NAME = "outboundImageQueue";
	public static final String ROUTING_IN_KEY = "images.in.key";
	public static final String ROUTING_OUT_KEY = "images.out.key";


	public static void main(String[] args) {
		SpringApplication.run(ImageDetectionDemoApplication.class, args);
	}

	@Bean
    public RabbitAdmin rabbitAdmin(final ConnectionFactory connectionFactory )
    {
        return new RabbitAdmin(connectionFactory);
    }
	
	@Bean
	public TopicExchange appExchange() {
		return new TopicExchange(EXCHANGE_NAME);
	}

	@Bean
	public Queue appInBoundQueue() {
		return new Queue(QUEUE_IN_NAME);
	}

	@Bean
	public Queue appOutBoundQueue() {
		return new Queue(QUEUE_OUT_NAME);
	}
	
	@Bean
	public Binding declareBindingInBound() {
		return BindingBuilder.bind(appInBoundQueue()).to(appExchange()).with(ROUTING_IN_KEY);
	}

	@Bean
	public Binding declareBindingOutBound() {
		return BindingBuilder.bind(appOutBoundQueue()).to(appExchange()).with(ROUTING_OUT_KEY);
	}
	
	@Bean
	public RabbitTemplate rabbitTemplate(final ConnectionFactory connectionFactory) {
		final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
		rabbitTemplate.setMessageConverter(producerJackson2MessageConverter());
		return rabbitTemplate;
	}
	
	@Bean
	public DefaultMessageHandlerMethodFactory messageHandlerMethodFactory() {
		DefaultMessageHandlerMethodFactory factory = new DefaultMessageHandlerMethodFactory();
		factory.setMessageConverter(consumerJackson2MessageConverter());
		return factory;
	}

	@Bean
	public Jackson2JsonMessageConverter producerJackson2MessageConverter() {
		return new Jackson2JsonMessageConverter();
	}

	@Bean
	public MappingJackson2MessageConverter consumerJackson2MessageConverter() {
		return new MappingJackson2MessageConverter();
	}


	@Override
	public void configureRabbitListeners(RabbitListenerEndpointRegistrar registrar) {
		registrar.setMessageHandlerMethodFactory(messageHandlerMethodFactory());
	}
	
	
}
