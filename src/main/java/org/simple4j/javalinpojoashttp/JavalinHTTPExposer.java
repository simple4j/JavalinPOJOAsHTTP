package org.simple4j.javalinpojoashttp;

import org.simple4j.wsfeeler.pojoashttp.HTTPExposer;
import org.springframework.context.ApplicationContext;

import io.javalin.Javalin;

public class JavalinHTTPExposer extends HTTPExposer
{

	public JavalinHTTPExposer(ApplicationContext context)
	{
		super(context);
	}

	@Override
	public void expose()
	{
		Javalin javalin = Javalin.create(config -> {
			config.routes.post(this.getUrlBase()+"/request.json", ctx -> 
	        {
	            String bodyJson = ctx.body();
	            ctx.result(processRequest(bodyJson));
	        });
		}).start(this.getListenerPortNumber());

	}

}
