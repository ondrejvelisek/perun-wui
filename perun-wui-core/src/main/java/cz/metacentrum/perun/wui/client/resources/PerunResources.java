package cz.metacentrum.perun.wui.client.resources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;

/**
 *
 *
 * @author Pavel Zlámal
 */
public interface PerunResources extends ClientBundle {

	PerunResources INSTANCE = GWT.create(PerunResources.class);

	@Source("perun.png")
	ImageResource getPerunLogo();
        
        @Source("PerunWui.gss")
        @CssResource.NotStrict
	public CssResource gss();

	@Source("perun-ico.png")
	public ImageResource getPerunIcon();

}
