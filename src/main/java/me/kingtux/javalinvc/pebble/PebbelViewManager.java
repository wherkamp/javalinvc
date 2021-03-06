package me.kingtux.javalinvc.pebble;

import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import io.javalin.http.Context;
import me.kingtux.javalinvc.JavalinVC;

import me.kingtux.javalinvc.rg.ResourceGrabber;
import me.kingtux.javalinvc.view.View;
import me.kingtux.javalinvc.view.ViewManager;
import me.kingtux.javalinvc.view.ViewVariableGrabber;


import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

public class PebbelViewManager implements ViewManager {
    private ResourceGrabber resourceGrabber;
    private Map<String, Object> defaultVariables = new HashMap<>();
    private Map<String, ViewVariableGrabber> viewVariableGrabbers = new HashMap<>();
    private String extension;
    private PebbleEngine pebbleEngine;

    public PebbelViewManager(JavalinVC website, String extension) {
        this.resourceGrabber = website.getResourceGrabber();
        // sr.host sr.protocol and sr.baseURL
        if (website != null) {
            registerDefaultViewVariable("sr", website.getRules());
        }
        pebbleEngine = new PebbleEngine.Builder().loader(new PebbleLoader(website)).extension(new JavalinVCExtension(website)).build();
        this.extension = extension == null || extension.isEmpty() ? ".html" : extension;

    }

    @Override
    public void setResourceGrabber(ResourceGrabber tg) {
        resourceGrabber = tg;
    }

    @Override
    public ResourceGrabber getResourceGrabber() {
        return resourceGrabber;
    }

    @Override
    public String parseView(ResourceGrabber grabber, View view) {
        PebbleView pebbleView = (PebbleView) view;
        PebbleTemplate compiledTemplate = pebbleEngine.getTemplate(view.getTemplate());
        Writer writer = new StringWriter();
        try {
            compiledTemplate.evaluate(writer, pebbleView.getValues());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return writer.toString();
    }

    @Override
    public View buildView(JavalinVC website, Context request) {
        View view = new PebbleView();
        defaultVariables.forEach(view::set);
        if (request != null)
            viewVariableGrabbers.forEach((s, viewVariableGrabber) -> view.set(s, viewVariableGrabber.get(request)));
        return view;
    }

    @Override
    public void registerDefaultViewVariable(String key, Object value) {
        defaultVariables.put(key, value);
    }

    @Override
    public void registerViewVariableGrabber(String key, ViewVariableGrabber value) {
        viewVariableGrabbers.put(key, value);
    }

    @Override
    public String getExtension() {
        return extension;
    }

    @Override
    public void setExtension(String extension) {
        this.extension = extension;
    }
}
