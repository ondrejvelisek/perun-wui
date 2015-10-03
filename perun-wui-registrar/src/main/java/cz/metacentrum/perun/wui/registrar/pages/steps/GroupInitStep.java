package cz.metacentrum.perun.wui.registrar.pages.steps;

import com.google.gwt.core.client.JavaScriptObject;
import cz.metacentrum.perun.wui.json.JsonEvents;
import cz.metacentrum.perun.wui.model.PerunException;
import cz.metacentrum.perun.wui.model.beans.Application;
import cz.metacentrum.perun.wui.model.common.PerunPrincipal;
import cz.metacentrum.perun.wui.registrar.model.RegistrarObject;
import cz.metacentrum.perun.wui.registrar.pages.FormView;

/**
 * Created by ondrej on 3.10.15.
 */
public class GroupInitStep extends FormStep {

	public GroupInitStep(FormView formView, Step next) {
		super(formView, next);
	}

	@Override
	public void call(final PerunPrincipal pp, final RegistrarObject registrar) {

		form.clear();
		form.setFormItems(registrar.getGroupFormInitial());
		form.setApp(Application.createNew(registrar.getVo(), registrar.getGroup(), Application.ApplicationType.INITIAL,
				getFedInfo(pp), pp.getActor(), pp.getExtSource(), pp.getExtSourceType(), pp.getExtSourceLoa()));
		form.setOnSubmitEvent(new JsonEvents() {

			@Override
			public void onFinished(JavaScriptObject jso) {
				if (mustRevalidateMail()) {
					formView.setRegisteredUnknownMail();
				}
				next.call(pp, registrar);
			}

			@Override
			public void onError(PerunException error) {
				formView.displayException(error);
			}

			@Override
			public void onLoadingStart() {

			}
		});
	}
}