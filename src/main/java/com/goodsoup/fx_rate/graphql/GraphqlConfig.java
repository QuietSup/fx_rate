package com.goodsoup.fx_rate.graphql;

import graphql.scalars.ExtendedScalars;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.graphql.execution.DataFetcherExceptionResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;

@Configuration
public class GraphqlConfig {

    @Bean
    RuntimeWiringConfigurer runtimeWiringConfigurer() {
        return wiringBuilder -> wiringBuilder.scalar(ExtendedScalars.GraphQLBigDecimal);
    }

    @Bean
    DataFetcherExceptionResolver dataFetcherExceptionResolver() {
        return DataFetcherExceptionResolver.forSingleError((ex, env) -> {
            if (ex instanceof ConstraintViolationException cve) {
                String msg = cve.getConstraintViolations().stream()
                        .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                        .sorted()
                        .reduce((a, b) -> a + "; " + b)
                        .orElse("Validation failed");

                return GraphqlErrorBuilder.newError(env)
                        .message(msg)
                        .errorType(ErrorType.BAD_REQUEST)
                        .build();
            }

            if (ex instanceof AccessDeniedException) {
                return GraphqlErrorBuilder.newError(env)
                        .message("Forbidden")
                        .errorType(ErrorType.FORBIDDEN)
                        .build();
            }

            if (ex instanceof AuthenticationCredentialsNotFoundException) {
                return GraphqlErrorBuilder.newError(env)
                        .message("Unauthorized")
                        .errorType(ErrorType.UNAUTHORIZED)
                        .build();
            }

            return null;
        });
    }
}

