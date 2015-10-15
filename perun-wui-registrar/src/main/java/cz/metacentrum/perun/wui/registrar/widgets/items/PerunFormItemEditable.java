package cz.metacentrum.perun.wui.registrar.widgets.items;

import com.google.gwt.user.client.ui.Widget;
import cz.metacentrum.perun.wui.model.beans.ApplicationFormItemData;
import cz.metacentrum.perun.wui.registrar.widgets.PerunForm;
import org.gwtbootstrap3.client.ui.Column;
import org.gwtbootstrap3.client.ui.FormLabel;
import org.gwtbootstrap3.client.ui.HelpBlock;
import org.gwtbootstrap3.client.ui.Row;
import org.gwtbootstrap3.client.ui.constants.ValidationState;
import org.gwtbootstrap3.client.ui.gwt.FlowPanel;
import org.gwtbootstrap3.extras.animate.client.ui.Animate;
import org.gwtbootstrap3.extras.animate.client.ui.constants.Animation;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents editable form items. E.g. TextField, Selectbox, ...
 *
 * @author Ondrej Velisek <ondrejvelisek@gmail.com>
 */
public abstract class PerunFormItemEditable extends PerunFormItem {

	private FormLabel label;
	private HelpBlock status;
	private HelpBlock help;

	public PerunFormItemEditable(ApplicationFormItemData item, String lang) {
		super(item, lang);
		setValidationTriggers();
	}

	@Override
	protected Widget initFormItem() {

		Row item = new Row();

		this.label = new FormLabel();
		FlowPanel widgetWithTexts = new FlowPanel();

		Row widgetWithStatus = new Row();
		this.help = new HelpBlock();

		Column widget = new Column(PerunForm.WIDGET_SIZE);
		this.status = new HelpBlock();


		item.add(label);
		item.add(widgetWithTexts);

		widgetWithTexts.add(widgetWithStatus);
		widgetWithTexts.add(help);

		widgetWithStatus.add(widget);
		widgetWithStatus.add(status);

		widget.add(initWidget());

		setPrefilledValue();


		label.setText(getLabelOrShortName());
		label.setShowRequiredIndicator(getItemData().getFormItem().isRequired());

		String helpText = getItemData().getFormItem().getItemTexts(getLang()).getHelp();
		help.setHTML(helpText);

		status.addStyleName(PerunForm.STATUS_SIZE.getCssName());
		status.setMarginTop(0);
		help.setMarginTop(0);
		label.addStyleName(PerunForm.LABEL_SIZE.getCssName());
		widgetWithTexts.addStyleName(PerunForm.WIDGET_WITH_TEXT_SIZE.getCssName());

		return item;
	}

	/**
	 * Generate form item widget. Widget means part of item which is editable.
	 * ithout label, helper and error texts.
	 *
	 * @return generated form item widget
	 */
	protected abstract Widget initWidget();

	/**
	 * @return the same widget as initWidget() method returns. But it does not generate it.
	 */
	protected abstract Widget getWidget();

	/**
	 * Set default validation triggers (handlers) on form item widget.
	 */
	protected abstract void setValidationTriggers();


	public void setPrefilledValue() {
		if (getItemData().getPrefilledValue() == null) {
			return;
		}
		if (getItemData().getPrefilledValue().equals("")) {
			return;
		}
		if (getItemData().getPrefilledValue().equals("null")) {
			return;
		}

		setValue(getItemData().getPrefilledValue());
	}


	/**
	 * Item is considered required only when "required" by definition TODO and user can modify value.
	 *
	 * @return TRUE if required / FALSE = not required
	 */
	public boolean isRequired() {

		return getItemData().getFormItem().isRequired();

	}


	/**
	 * Status of item means validationState and text.
	 * This method display to (inform) user visually status of validation.
	 *
	 * @param text
	 * @param state
	 */
	public void setStatus(String text, ValidationState state) {
		if (text.isEmpty()) {
			this.status.setVisible(false);
		} else {
			this.status.setVisible(true);
		}
		if (state.equals(ValidationState.ERROR)
		|| state.equals(ValidationState.WARNING)) {
			Animate.animate(status, Animation.SHAKE, 1, 700);
		}
		this.status.setHTML(text);
		this.setValidationState(state);
	}

	public void setStatus(ValidationState state) {
		setStatus("", state);
	}

	public void unsetStatus() {
		setStatus(ValidationState.NONE);
	}



	/**
	 * Parses the "options" into MAP
	 *
	 * Standard HTML options (e.g. for selection box) are in for each locale in ItemTexts.label separated by | with values separated by #.
	 * Example:
	 * Thus a language selection box would have for English locale the label <code>cs#Czech|en#English</code>.
	 *
	 * @return Map with key/value pairs of options
	 */
	public Map<String, String> parseItemOptions(){

		String options = getItemData().getFormItem().getItemTexts(getLang()).getOptions();

		Map<String, String> map = new HashMap<String, String>();

		if (options == null) {
			return map;
		}
		if (options.equals("")) {
			return map;
		}

		String[] keyValue = options.split("\\|");

		for(int i = 0; i < keyValue.length; i++){

			String kv = keyValue[i];

			String[] split = kv.split("#", 2);

			if(split.length != 2){
				continue;
			}

			String key = split[0];
			String value = split[1];
			map.put(key, value);
		}
		return map;
	}
}