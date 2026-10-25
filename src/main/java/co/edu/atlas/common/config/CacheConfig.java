package co.edu.atlas.common.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.filter.ShallowEtagHeaderFilter;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Cache del atlas para el modo offline: ETag en las lecturas del atlas y Cache-Control segun el tipo de dato.
 * Catalogos: 1 h; fichas y listados: siempre revalidar (If-None-Match devuelve 304). El resto de la API conserva
 * el no-store de Spring Security, y los errores se responden con no-store.
 */
@Configuration
public class CacheConfig implements WebMvcConfigurer {

    private static final CacheControl CATALOGO = CacheControl.maxAge(Duration.ofHours(1)).cachePrivate();
    private static final CacheControl ATLAS = CacheControl.maxAge(Duration.ZERO).mustRevalidate().cachePrivate();

    @Bean
    FilterRegistrationBean<ShallowEtagHeaderFilter> etagAtlas() {
        FilterRegistrationBean<ShallowEtagHeaderFilter> registro = new FilterRegistrationBean<>(new ShallowEtagHeaderFilter());
        registro.addUrlPatterns("/regiones", "/regiones/*", "/categorias", "/ejercicios", "/ejercicios/*");
        registro.setName("etagAtlas");
        return registro;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(cabecera(CATALOGO)).addPathPatterns("/regiones", "/regiones/**", "/categorias");
        registry.addInterceptor(cabecera(ATLAS)).addPathPatterns("/ejercicios", "/ejercicios/**");
    }

    private static HandlerInterceptor cabecera(CacheControl cacheControl) {
        return new HandlerInterceptor() {
            @Override
            public void postHandle(
                    HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
                // postHandle no se invoca si el controlador lanzo una excepcion: los errores nunca quedan cacheables.
                // La respuesta aun no se confirma porque ShallowEtagHeaderFilter la almacena en memoria.
                if (HttpMethod.GET.matches(request.getMethod()) && !response.isCommitted()) {
                    response.setHeader(HttpHeaders.CACHE_CONTROL, cacheControl.getHeaderValue());
                    response.addHeader(HttpHeaders.VARY, HttpHeaders.AUTHORIZATION);
                }
            }
        };
    }
}
