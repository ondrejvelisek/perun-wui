package cz.metacentrum.perun.wui.admin.pages.perunManagement;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;
import cz.metacentrum.perun.wui.client.utils.JsUtils;
import cz.metacentrum.perun.wui.client.utils.UiUtils;
import cz.metacentrum.perun.wui.json.JsonEvents;
import cz.metacentrum.perun.wui.json.managers.ServicesManager;
import cz.metacentrum.perun.wui.model.PerunException;
import cz.metacentrum.perun.wui.model.beans.Service;
import cz.metacentrum.perun.wui.model.columnProviders.ServiceColumnProvider;
import cz.metacentrum.perun.wui.pages.FocusableView;
import cz.metacentrum.perun.wui.widgets.PerunButton;
import cz.metacentrum.perun.wui.widgets.PerunDataGrid;
import cz.metacentrum.perun.wui.widgets.boxes.ExtendedSuggestBox;
import cz.metacentrum.perun.wui.widgets.resources.PerunButtonType;
import cz.metacentrum.perun.wui.widgets.resources.UnaccentMultiWordSuggestOracle;
import org.gwtbootstrap3.client.ui.*;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.extras.growl.client.ui.Growl;
import org.gwtbootstrap3.extras.growl.client.ui.GrowlType;

/**
 * PERUN ADMIN - SERVICES MANAGEMENT VIEW
 *
 * @author Kristyna Kysela
 */
public class ServicesManagementView extends ViewImpl implements ServicesManagementPresenter.MyView, FocusableView {

	private UnaccentMultiWordSuggestOracle oracle = new UnaccentMultiWordSuggestOracle();

	@UiField(provided = true)
	PerunDataGrid<Service> grid;

	@UiField (provided = true)
	PerunButton remove;

	@UiField(provided = true)
	ExtendedSuggestBox textBox = new ExtendedSuggestBox(oracle);

	@UiField ButtonToolBar menu;
	@UiField PerunButton filterButton;
	@UiField PerunButton addButton;

	ServicesManagementView view = this;

	interface ServicesManagementViewUiBinder extends UiBinder<Widget, ServicesManagementView> {
	}

	@Inject
	ServicesManagementView(final ServicesManagementViewUiBinder uiBinder) {

		grid = new PerunDataGrid<Service>(new ServiceColumnProvider());
		remove = PerunButton.getButton(PerunButtonType.REMOVE, ButtonType.DANGER, "Remove selected service(s)");

		initWidget(uiBinder.createAndBindUi(this));
		UiUtils.bindFilterBox(grid, textBox, filterButton);
		UiUtils.bindTableLoading(grid, filterButton, true);
		UiUtils.bindTableLoading(grid, textBox, true);
		UiUtils.bindTableSelection(grid, remove);

		draw();
	}

	@UiHandler(value = "remove")
	public void removeOnClick(ClickEvent event) {

		for (final Service service : grid.getSelectedList()) {
			ServicesManager.deleteService(service, new JsonEvents() {

				JsonEvents loadAgain = this;

				@Override
				public void onFinished(JavaScriptObject jso) {
					remove.setProcessing(false);
					grid.removeFromTable(service);
					Growl.growl("Service " + service.getName() + " was deleted.", GrowlType.SUCCESS);
				}

				@Override
				public void onError(PerunException error) {
					remove.setProcessing(false);
					Growl.growl("Service "+service.getName()+" was not deleted - " + error.getMessage() + ".", GrowlType.DANGER);
					grid.getLoaderWidget().onError(error, new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							ServicesManager.deleteService(service, loadAgain);
						}
					});
				}

				@Override
				public void onLoadingStart() {
					remove.setProcessing(true);
				}
			});

		}
	}

	@UiHandler(value = "addButton")
	public void onClick(ClickEvent event) {

	}

	public void draw() {

		ServicesManager.getServices(new JsonEvents() {

			JsonEvents loadAgain = this;

			@Override
			public void onFinished(JavaScriptObject jso) {
				grid.setList(JsUtils.<Service>jsoAsList(jso));
			}

			@Override
			public void onError(PerunException error) {
				grid.getLoaderWidget().onError(error, new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						ServicesManager.getServices(loadAgain);
					}
				});
			}

			@Override
			public void onLoadingStart() {
				grid.clearTable();
				grid.getLoaderWidget().onLoading();
			}
		});

	}

	@Override
	public void focus() {
		textBox.setFocus(true);
	}



}