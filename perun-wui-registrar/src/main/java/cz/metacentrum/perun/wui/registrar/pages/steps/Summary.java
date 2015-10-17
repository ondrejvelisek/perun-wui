package cz.metacentrum.perun.wui.registrar.pages.steps;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import cz.metacentrum.perun.wui.model.PerunException;
import cz.metacentrum.perun.wui.model.beans.Application;
import cz.metacentrum.perun.wui.model.common.PerunPrincipal;
import cz.metacentrum.perun.wui.registrar.client.RegistrarTranslation;
import cz.metacentrum.perun.wui.registrar.model.RegistrarObject;
import cz.metacentrum.perun.wui.registrar.pages.FormView;
import cz.metacentrum.perun.wui.widgets.PerunButton;
import cz.metacentrum.perun.wui.widgets.resources.PerunButtonType;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.ListGroup;
import org.gwtbootstrap3.client.ui.ListGroupItem;
import org.gwtbootstrap3.client.ui.constants.HeadingSize;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.html.Text;

/**
 * Represents a final step in registration process.
 *
 * @author Ondrej Velisek <ondrejvelisek@gmail.com>
 */
public class Summary implements Step {

	private Application.ApplicationType voApplication;
	private Application.ApplicationType groupApplication;
	private FormView formView;
	private RegistrarTranslation translation;

	public Summary(FormView formView, Application.ApplicationType voApplication, Application.ApplicationType groupApplication) {
		this.formView = formView;
		this.translation = formView.getTranslation();
		this.voApplication = voApplication;
		this.groupApplication = groupApplication;
	}

	@Override
	public void call(final PerunPrincipal pp, final RegistrarObject registrar) {

		formView.getForm().clear();
		displaySummaryTitle(registrar);
		displaySummaryMessage(registrar);
		displayContinueButton(registrar, (groupApplication != null) ? groupApplication : voApplication);

	}

	private void displaySummaryTitle(RegistrarObject registrar) {
		Heading title = new Heading(HeadingSize.H2);
		Text text = new Text();

		if (voApplication == null && groupApplication == null) {

			//text.setText(translation.canDoNothing());

		} else {
			Icon success = new Icon(IconType.CHECK_CIRCLE);
			success.setColor("#5cb85c");
			title.add(success);
			if (groupApplication == Application.ApplicationType.INITIAL) {

				if (registrar.hasGroupFormAutoApproval()) {
					text.setText(" "+translation.initTitleAutoApproval());
				} else {
					text.setText(" "+translation.initTitle());
				}

			} else if (voApplication == Application.ApplicationType.EXTENSION) {

				if (registrar.hasVoFormAutoApprovalExtension()) {
					text.setText(" "+translation.extendTitleAutoApproval());
				} else {
					text.setText(" "+translation.extendTitle());
				}

			} else if (voApplication == Application.ApplicationType.INITIAL) {

				if (registrar.hasVoFormAutoApproval()) {
					text.setText(" "+translation.initTitleAutoApproval());
				} else {
					text.setText(" "+translation.initTitle());
				}

			}
		}
		title.add(text);
		formView.getForm().add(title);
	}

	private void displaySummaryMessage(RegistrarObject registrar) {

		if (voApplication == null && groupApplication == null) {

			if (registrar.getGroupFormInitialException() != null) {
				switch (registrar.getGroupFormInitialException().getName()) {
					case "DuplicateRegistrationAttemptException":
						displayException(registrar.getGroupFormInitialException());
						break;
					case "AlreadyRegisteredException":
						displayException(registrar.getGroupFormInitialException());
						break;
					default:
						displayException(registrar.getGroupFormInitialException());
				}
			} else if (registrar.getVoFormExtensionException() != null) {
				switch (registrar.getVoFormExtensionException().getName()) {
					case "ExtendMembershipException":
						displayException(registrar.getVoFormExtensionException());
						break;
					case "DuplicateRegistrationAttemptException":
						registrar.getVoFormExtensionException().setName("DuplicateExtensionAttemptException");
						displayException(registrar.getVoFormExtensionException());
						break;
					case "MemberNotExistsException":
						if (registrar.getVoFormInitialException() != null) {
							displayVoFormInitialException(registrar);
						}
						break;
					default:
						displayException(registrar.getVoFormExtensionException());
				}
			} else if (registrar.getVoFormInitialException() != null) {
				displayVoFormInitialException(registrar);
			}

		} else {
			ListGroup message = new ListGroup();
			ListGroupItem groupStat = new ListGroupItem();
			if (groupApplication == Application.ApplicationType.INITIAL) {

				if (registrar.hasGroupFormAutoApproval()) {
					groupStat.setText(translation.registered(registrar.getGroup().getName()));
				} else {
					groupStat.setText(translation.waitForAcceptation(registrar.getGroup().getName()));
				}
				message.add(groupStat);
			}
			ListGroupItem voStat = new ListGroupItem();
			if (voApplication == Application.ApplicationType.EXTENSION) {

				if (registrar.hasVoFormAutoApprovalExtension()) {
					voStat.setText(translation.extended(registrar.getVo().getName()));
				} else {
					voStat.setText(translation.waitForExtAcceptation(registrar.getVo().getName()));
				}
				message.add(voStat);

			} else if (voApplication == Application.ApplicationType.INITIAL) {

				if (registrar.hasVoFormAutoApproval()) {
					voStat.setText(translation.registered(registrar.getVo().getName()));
				} else {
					voStat.setText(translation.waitForAcceptation(registrar.getVo().getName()));
				}
				message.add(voStat);

			}
			if (groupApplication == null) {

				if (registrar.getGroupFormInitialException() != null) {
					switch (registrar.getGroupFormInitialException().getName()) {
						case "DuplicateRegistrationAttemptException":
							groupStat.setText(translation.groupFailedAlreadyApplied(registrar.getGroup().getName()));
							break;
						case "AlreadyRegisteredException":
							groupStat.setText(translation.groupFailedAlreadyRegistered(registrar.getGroup().getName()));
							break;
						default:
							groupStat.setText(registrar.getGroupFormInitialException().getMessage());
					}
					message.add(groupStat);
				}
			}
			if (formView.getRegisteredUnknownMail()) {
				ListGroupItem verifyMail = new ListGroupItem();
				verifyMail.add(new Icon(IconType.WARNING));
				verifyMail.add(new Text(" " + translation.verifyMail()));
				message.add(verifyMail);
			}
			formView.getForm().add(message);
		}

	}

	private void displayVoFormInitialException(RegistrarObject registrar) {
		switch (registrar.getVoFormInitialException().getName()) {
			case "DuplicateRegistrationAttemptException":
				displayException(registrar.getVoFormInitialException());
				break;
			case "AlreadyRegisteredException":
				displayException(registrar.getVoFormInitialException());
				break;
			default:
				displayException(registrar.getVoFormInitialException());
		}
	}

	private void displayContinueButton(RegistrarObject registrar, Application.ApplicationType redirect) {

		PerunButton cont;
		if (redirect == null) {

			if (Window.Location.getParameter("targetexisting") != null) {
				if (registrar.getGroup() != null) {
					PerunException pEx = registrar.getGroupFormInitialException();
					if (pEx != null) {
						if (pEx.getName().equals("DuplicateRegistrationAttemptException")
								|| pEx.getName().equals("AlreadyRegisteredException")) {
							cont = PerunButton.getButton(PerunButtonType.CONTINUE, new ClickHandler() {
								@Override
								public void onClick(ClickEvent event) {
									Window.Location.assign(Window.Location.getParameter("targetexisting"));
								}
							});
							formView.getForm().add(cont);
						}
					}
				} else {
					PerunException pEx = registrar.getVoFormInitialException();
					if (pEx != null) {
						if (pEx.getName().equals("DuplicateRegistrationAttemptException")
								|| pEx.getName().equals("AlreadyRegisteredException")) {
							cont = PerunButton.getButton(PerunButtonType.CONTINUE, new ClickHandler() {
								@Override
								public void onClick(ClickEvent event) {
									Window.Location.assign(Window.Location.getParameter("targetexisting"));
								}
							});
							formView.getForm().add(cont);
						}
					}
				}
			}

		} else {
			switch (redirect) {
				case INITIAL:

					if (Window.Location.getParameter("targetnew") != null) {
						cont = PerunButton.getButton(PerunButtonType.CONTINUE, new ClickHandler() {
							@Override
							public void onClick(ClickEvent event) {
								Window.Location.assign(Window.Location.getParameter("targetnew"));
							}
						});
						formView.getForm().add(cont);
					}

					break;
				case EXTENSION:

					if (Window.Location.getParameter("targetextended") != null) {
						cont = PerunButton.getButton(PerunButtonType.CONTINUE, new ClickHandler() {
							@Override
							public void onClick(ClickEvent event) {
								Window.Location.assign(Window.Location.getParameter("targetextended"));
							}
						});
						formView.getForm().add(cont);
					}

					break;
			}
		}

	}

	private void displayException(PerunException ex) {
		formView.displayException(ex);
	}
}