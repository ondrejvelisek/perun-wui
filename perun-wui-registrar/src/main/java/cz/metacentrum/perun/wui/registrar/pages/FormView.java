package cz.metacentrum.perun.wui.registrar.pages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;
import cz.metacentrum.perun.wui.client.resources.PerunSession;
import cz.metacentrum.perun.wui.client.utils.Utils;
import cz.metacentrum.perun.wui.json.JsonEvents;
import cz.metacentrum.perun.wui.json.managers.RegistrarManager;
import cz.metacentrum.perun.wui.model.BasicOverlayObject;
import cz.metacentrum.perun.wui.model.PerunException;
import cz.metacentrum.perun.wui.model.beans.ApplicationFormItemData;
import cz.metacentrum.perun.wui.model.beans.Attribute;
import cz.metacentrum.perun.wui.model.beans.ExtSource;
import cz.metacentrum.perun.wui.model.beans.Group;
import cz.metacentrum.perun.wui.model.beans.Identity;
import cz.metacentrum.perun.wui.model.beans.Vo;
import cz.metacentrum.perun.wui.model.common.PerunPrincipal;
import cz.metacentrum.perun.wui.registrar.client.RegistrarTranslation;
import cz.metacentrum.perun.wui.registrar.model.RegistrarObject;
import cz.metacentrum.perun.wui.registrar.pages.steps.GroupInitStep;
import cz.metacentrum.perun.wui.registrar.pages.steps.Summary;
import cz.metacentrum.perun.wui.registrar.pages.steps.VoExtOfferStep;
import cz.metacentrum.perun.wui.registrar.pages.steps.VoExtStep;
import cz.metacentrum.perun.wui.registrar.pages.steps.VoInitStep;
import cz.metacentrum.perun.wui.registrar.widgets.PerunForm;
import cz.metacentrum.perun.wui.widgets.PerunLoader;
import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.ButtonGroup;
import org.gwtbootstrap3.client.ui.DropDownMenu;
import org.gwtbootstrap3.client.ui.Image;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.ModalBody;
import org.gwtbootstrap3.client.ui.ModalFooter;
import org.gwtbootstrap3.client.ui.constants.AlertType;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.IconPosition;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.constants.ModalBackdrop;
import org.gwtbootstrap3.client.ui.constants.Toggle;
import org.gwtbootstrap3.client.ui.html.Paragraph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static cz.metacentrum.perun.wui.model.beans.Application.ApplicationType;

/**
 * View for displaying registration form of VO / Group
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class FormView extends ViewImpl implements FormPresenter.MyView {

	interface FormViewUiBinder extends UiBinder<Widget, FormView> {
	}

	private FormView formView = this;

	private RegistrarTranslation translation = GWT.create(RegistrarTranslation.class);

	private Vo vo;
	private Group group;
	private boolean registeredUnknownMail;

	@UiField
	PerunForm form;

	@UiField
	Image logo;

	@UiField
	Alert notice;

	private PerunException displayedException;

	@Inject
	public FormView(FormViewUiBinder binder) {
		initWidget(binder.createAndBindUi(this));
		draw();
	}


	public PerunForm getForm() {
		return form;
	}

	public RegistrarTranslation getTranslation() {
		return translation;
	}

	public void setRegisteredUnknownMail() {
		registeredUnknownMail = true;
	}

	public boolean getRegisteredUnknownMail() {
		return registeredUnknownMail;
	}



	public void draw() {

		final PerunLoader loader = new PerunLoader();
		form.add(loader);

		final String voName = Window.Location.getParameter("vo");
		final String groupName = Window.Location.getParameter("group");

		Scheduler.get().scheduleDeferred(new Command() {
			@Override
			public void execute() {
				loader.getWidget().getElement().getFirstChildElement().setAttribute("style", "height: "+ (Window.getClientHeight()-100)+"px;");
			}
		});

		if (voName == null || voName.isEmpty()) {
			this.displayedException = PerunException.createNew("0", "WrongURL", "Missing parameters in URL.");
			displayException(loader, displayedException);
			return;
		}

		final PerunPrincipal pp = PerunSession.getInstance().getPerunPrincipal();

		RegistrarManager.initializeRegistrar(voName, groupName, new JsonEvents() {

			JsonEvents retry = this;

			@Override
			public void onFinished(JavaScriptObject jso) {

				final RegistrarObject registrar = jso.cast();

				// recreate VO and group
				vo = registrar.getVo();

				if (groupName != null && !groupName.isEmpty()) {
					group = registrar.getGroup();
				}

				ArrayList<Attribute> attrList = registrar.getVoAttributes();
				for (Attribute a : attrList) {
					if (a.getFriendlyName().equals("voLogoURL")) {
						logo.setUrl(a.getValue());
						logo.setVisible(true);
					}
				}



				if (registrar.getException() != null) {
					GWT.log("Exception " + registrar.getException().getMessage());
					if (registrar.getException().getName().equals("VoNotExistsException") ||
							registrar.getException().getName().equals("GroupNotExistsException") ||
							registrar.getException().getName().equals("FormNotExistsException")) {
						displayException(loader, registrar.getException());
						return;
					}

					// SEVERE EXCEPTION - DO NOT LOAD FORM
					loader.onError(registrar.getException(), new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							RegistrarManager.initializeRegistrar(voName, groupName, retry);
						}
					});

				} else {

					loader.onFinished();
					loader.removeFromParent();

					if (isApplyingToGroup(registrar)) {

						if (groupFormExists(registrar)) {

							if (voInitialFormExists(registrar)) {

								(new VoInitStep(formView,
										new GroupInitStep(formView,
												new Summary(formView, ApplicationType.INITIAL, ApplicationType.INITIAL)
										))).call(pp, registrar);

							} else if (voExtensionFormExists(registrar)) {

								(new VoExtOfferStep(formView,
										new VoExtStep(formView,
												new GroupInitStep(formView,
														new Summary(formView, ApplicationType.EXTENSION, ApplicationType.INITIAL))),
										new GroupInitStep(formView,
												new Summary(formView, null, ApplicationType.INITIAL)
										))).call(pp, registrar);

							} else {

								(new GroupInitStep(formView,
										new Summary(formView, null, ApplicationType.INITIAL)
								)).call(pp, registrar);

							}

						} else {

							if (voInitialFormExists(registrar)) {

								(new VoInitStep(formView,
										new Summary(formView, ApplicationType.INITIAL, null)
								)).call(pp, registrar);

							} else if (voExtensionFormExists(registrar)) {

								(new VoExtOfferStep(formView,
										new VoExtStep(formView,
												new Summary(formView, ApplicationType.EXTENSION, null)),
										new Summary(formView, null, null))).call(pp, registrar);

							} else {

								(new Summary(formView, null, null)).call(pp, registrar);

							}

						}

					} else {

						if (voInitialFormExists(registrar)) {

							(new VoInitStep(formView,
									new Summary(formView, ApplicationType.INITIAL, null)
							)).call(pp, registrar);

						} else if (voExtensionFormExists(registrar)) {

							(new VoExtStep(formView,
									new Summary(formView, ApplicationType.EXTENSION, null)
							)).call(pp, registrar);

						} else {

							(new Summary(formView, null, null)).call(pp, registrar);

						}

					}

					// CHECK SIMILAR USERS
					if (!registrar.getSimilarUsers().isEmpty()) {
						showSimilarUsersDialog(registrar);
					}
				}

			}

			@Override
			public void onError(PerunException error) {
				loader.onError(error, new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						RegistrarManager.initializeRegistrar(voName, groupName, retry);
					}
				});
			}

			@Override
			public void onLoadingStart() {
				loader.onLoading();
			}
		});

	}



	private boolean voInitialFormExists(RegistrarObject ro) {
		return !ro.getVoFormInitial().isEmpty();
	}

	private boolean voExtensionFormExists(RegistrarObject ro) {
		return !ro.getVoFormExtension().isEmpty();
	}

	private boolean groupFormExists(RegistrarObject ro) {
		return !ro.getGroupFormInitial().isEmpty();
	}

	private boolean isApplyingToGroup(RegistrarObject ro) {
		return (group != null);
	}



	public void displayException(PerunException ex) {
		displayException(null, ex);
	}

	public void displayException(PerunLoader loader, PerunException ex) {

		this.displayedException = ex;
		notice.setType(AlertType.WARNING);

		Button continueButton = new Button(translation.continueButton());
		continueButton.setIcon(IconType.CHEVRON_RIGHT);
		continueButton.setType(ButtonType.WARNING);
		continueButton.setIconPosition(IconPosition.RIGHT);
		continueButton.setMarginTop(20);

		if (ex.getName().equals("ExtendMembershipException")) {

			if (ex.getReason().equals("OUTSIDEEXTENSIONPERIOD")) {

				String exceptionText = "<i>unlimited</i>";
				if (ex.getExpirationDate() != null) exceptionText = ex.getExpirationDate().split(" ")[0];
				notice.getElement().setInnerHTML("<h4>"+translation.cantExtendMembership()+"</h4><p>"+translation.cantExtendMembershipOutside(exceptionText));

				if (Window.Location.getParameter("targetexisting") != null) {

					continueButton.addClickHandler(new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							Window.Location.replace(Window.Location.getParameter("targetexisting"));
						}
					});
					notice.add(continueButton);

				}

			} else if (ex.getReason().equals("NOUSERLOA")) {

				notice.getElement().setInnerHTML("<h4>"+translation.cantBecomeMember((group != null) ? group.getShortName() : vo.getName())+"</h4><p>"+translation.cantBecomeMemberLoa(Utils.translateIdp(PerunSession.getInstance().getPerunPrincipal().getExtSource())));

			} else if (ex.getReason().equals("INSUFFICIENTLOA")) {

				notice.getElement().setInnerHTML("<h4>"+translation.cantBecomeMember((group != null) ? group.getShortName() : vo.getName())+"</h4><p>"+translation.cantBecomeMemberInsufficientLoa(Utils.translateIdp(PerunSession.getInstance().getPerunPrincipal().getExtSource())));

			} else if (ex.getReason().equals("INSUFFICIENTLOAFOREXTENSION")) {

				notice.getElement().setInnerHTML("<h4>"+translation.cantExtendMembership()+"</h4><p>"+translation.cantExtendMembershipInsufficientLoa(Utils.translateIdp(PerunSession.getInstance().getPerunPrincipal().getExtSource())));

			}

		} else if (ex.getName().equals("AlreadyRegisteredException")) {

			notice.getElement().setInnerHTML("<h4>"+translation.alreadyRegistered((group != null) ? group.getShortName() : vo.getName())+"</h4>");

			if (Window.Location.getParameter("targetexisting") != null) {

				continueButton.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						Window.Location.replace(Window.Location.getParameter("targetexisting"));
					}
				});
				notice.add(continueButton);

			}

		} else if (ex.getName().equals("DuplicateRegistrationAttemptException")) {

			notice.getElement().setInnerHTML("<h4>"+ translation.alreadySubmitted(((group != null) ? group.getShortName() : vo.getName())) + "</h4><p>"+translation.visitSubmitted(Window.Location.getHref().split("#")[0], translation.submittedTitle()));

			if (Window.Location.getParameter("targetnew") != null) {

				continueButton.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						Window.Location.replace(Window.Location.getParameter("targetnew"));
					}
				});
				notice.add(continueButton);

			}

		} else if (ex.getName().equals("DuplicateExtensionAttemptException")) {

			notice.getElement().setInnerHTML("<h4>"+ translation.alreadySubmittedExtension((vo.getName())) + "</h4><p>"+translation.visitSubmitted(Window.Location.getHref().split("#")[0], translation.submittedTitle()));

			if (Window.Location.getParameter("targetextended") != null) {

				continueButton.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						Window.Location.replace(Window.Location.getParameter("targetextended"));
					}
				});
				notice.add(continueButton);

			}

		} else if (ex.getName().equalsIgnoreCase("MissingRequiredDataException")) {

			String missingItems = "<p><ul>";
			if (!ex.getFormItems().isEmpty()) {
				for (ApplicationFormItemData item : ex.getFormItems()) {
					missingItems += "<li>" + translation.missingAttribute(item.getFormItem().getFederationAttribute());
				}
			}
			missingItems += "<ul/>";

			notice.getElement().setInnerHTML(translation.missingRequiredData(Utils.translateIdp(PerunSession.getInstance().getPerunPrincipal().getExtSource())) + missingItems);

		} else if (ex.getName().equalsIgnoreCase("VoNotExistsException")) {

			notice.getElement().setInnerHTML("<h4>"+translation.voNotExistsException(Window.Location.getParameter("vo"))+"</h4>");

		} else if (ex.getName().equalsIgnoreCase("GroupNotExistsException")) {

			notice.getElement().setInnerHTML("<h4>"+translation.groupNotExistsException(Window.Location.getParameter("group"))+"</h4>");

		} else if (ex.getName().equalsIgnoreCase("WrongURL")) {

			notice.getElement().setInnerHTML("<h4>"+translation.missingVoInURL()+"</h4>");

		} else if (ex.getName().equalsIgnoreCase("FormNotExistsException")) {

			notice.getElement().setInnerHTML("<h4>"+translation.formNotExist()+"</h4>");

		} else if (ex.getName().equalsIgnoreCase("ApplicationNotCreatedException")) {

			notice.getElement().setInnerHTML("<h4>"+translation.applicationNotCreated()+"</h4>");

		} else  if (ex.getName().equalsIgnoreCase("RpcException")) {

			notice.getElement().setInnerHTML("<h4>"+translation.applicationNotCreated()+"</h4>");

		} else  if (ex.getName().equalsIgnoreCase("RegistrarException")) {

			notice.getElement().setInnerHTML("<h4>"+translation.registrarException()+"</h4>");

		} else {

			notice.getElement().setInnerHTML("<h4>"+translation.registrarException()+"</h4>");

		}

		Window.scrollTo(0, 0);

		notice.setVisible(true);
		if (loader != null) {
			loader.onFinished();
			loader.removeFromParent();
		}

	}



	private void showSimilarUsersDialog(RegistrarObject object) {

		final Modal modal = new Modal();
		modal.setTitle(object.getSimilarUsers().size() > 1 ? translation.similarUsersFoundTitle() : translation.similarUserFoundTitle());
		modal.setFade(true);
		modal.setDataKeyboard(false);
		modal.setDataBackdrop(ModalBackdrop.STATIC);
		modal.setClosable(false);
		modal.setWidth("700px");

		FlexTable ft = new FlexTable();
		ft.setWidth("100%");
		ft.setCellSpacing(5);

		int row = 0;
		for (Identity identity : object.getSimilarUsers()) {

			ft.setHTML(row, 0, "<strong>"+identity.getName()+"</strong><br />"+
					identity.getOrganization()+"<br />"+identity.getEmail());
			ft.getFlexCellFormatter().setWidth(row, 0, "200px");

			ButtonGroup certGroup = new ButtonGroup();
			DropDownMenu menu = getCertificatesJoinButton(certGroup);

			ButtonGroup idpGroup = new ButtonGroup();
			DropDownMenu menu2 = getIdpJoinButton(idpGroup);

			boolean certFound = false;
			boolean idpFound = false;

			ArrayList<ExtSource> sources = identity.getExternalIdentities();
			Collections.sort(sources, new Comparator<ExtSource>() {
				@Override
				public int compare(ExtSource o1, ExtSource o2) {
					return Utils.convertCertCN(Utils.translateIdp(o1.getName())).compareTo(Utils.convertCertCN(Utils.translateIdp(o2.getName())));
				}
			});

			for (final ExtSource source : sources) {

				if (source.getType().equals(ExtSource.ExtSourceType.X509.getType())) {

					AnchorListItem link = new AnchorListItem(Utils.convertCertCN(source.getName()));
					menu.add(link);
					link.addClickHandler(new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							RegistrarManager.getConsolidatorToken(new JsonEvents() {
								@Override
								public void onFinished(JavaScriptObject jso) {
									String token = ((BasicOverlayObject) jso).getString();
									Window.Location.replace(Utils.getIdentityConsolidatorLink("cert", true) + "&token=" + token);
								}

								@Override
								public void onError(PerunException error) {

								}

								@Override
								public void onLoadingStart() {

								}
							});
						}
					});

					if (!certFound) ft.setWidget(row, 1, certGroup);
					certFound = true;

				} else if (source.getType().equals(ExtSource.ExtSourceType.IDP.getType())) {

					AnchorListItem link = new AnchorListItem(Utils.translateIdp(source.getName()));
					menu2.add(link);
					link.addClickHandler(new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {

							RegistrarManager.getConsolidatorToken(new JsonEvents() {
								@Override
								public void onFinished(JavaScriptObject jso) {
									// FINAL URL must logout from SP, login to SP using specified IdP, redirect to IC and after that return to application form
									String token = ((BasicOverlayObject) jso).getString();
									String consolidatorUrl = Utils.getIdentityConsolidatorLink("fed", true)+URL.encodeQueryString("&token="+token);
									String redirectUrl = Utils.getWayfSpLogoutUrl() + "?return=" + Utils.getWayfSpDsUrl() + URL.encodeQueryString("?entityID=" + source.getName()+"&target="+consolidatorUrl);
									Window.Location.replace(redirectUrl);
								}

								@Override
								public void onError(PerunException error) {

								}

								@Override
								public void onLoadingStart() {

								}
							});
						}
					});
					if (!idpFound) ft.setWidget(row, 2, idpGroup);
					idpFound = true;

				}

			}

			row++;

		}

		ModalBody body = new ModalBody();
		body.add(new Paragraph(translation.similarUsersFound()));
		body.add(ft);
		modal.add(body);

		ModalFooter footer = new ModalFooter();

		final Button noThanks = new Button("...5...", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				modal.hide();
			}
		});
		noThanks.setType(ButtonType.DANGER);
		noThanks.setEnabled(false);

		Scheduler.get().scheduleFixedDelay(new Scheduler.RepeatingCommand() {
			int counter = 5;
			@Override
			public boolean execute() {
				if (counter <= 0) {
					noThanks.setText(translation.noThanks());
					noThanks.setEnabled(true);
					return false;
				}
				counter--;
				noThanks.setText("..."+counter+"...");
				return true;
			}
		}, 1000);
		footer.add(noThanks);

		modal.add(footer);
		modal.show();

	}


	private DropDownMenu getCertificatesJoinButton(ButtonGroup certGroup) {

		Button certButt = new Button(translation.byCertificate(), IconType.CERTIFICATE, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {

			}
		});
		certButt.setPaddingLeft(10);
		certButt.setIconFixedWidth(true);
		certButt.setType(ButtonType.SUCCESS);
		certButt.setDataToggle(Toggle.DROPDOWN);
		certGroup.add(certButt);
		DropDownMenu certMenu = new DropDownMenu();
		certGroup.add(certMenu);

		return certMenu;

	}

	private DropDownMenu getIdpJoinButton(ButtonGroup certGroup) {

		Button idpButt = new Button(translation.byIdp(), IconType.USER, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {

			}
		});
		idpButt.setPaddingLeft(10);
		idpButt.setIconFixedWidth(true);
		idpButt.setType(ButtonType.SUCCESS);
		idpButt.setDataToggle(Toggle.DROPDOWN);
		certGroup.add(idpButt);
		DropDownMenu idpMenu = new DropDownMenu();
		certGroup.add(idpMenu);

		return idpMenu;

	}

}