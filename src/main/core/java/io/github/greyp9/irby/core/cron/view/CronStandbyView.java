package io.github.greyp9.irby.core.cron.view;

import io.github.greyp9.arwo.core.action.ActionButtons;
import io.github.greyp9.arwo.core.action.ActionFactory;
import io.github.greyp9.arwo.core.alert.Alert;
import io.github.greyp9.arwo.core.alert.AlertU;
import io.github.greyp9.arwo.core.alert.Alerts;
import io.github.greyp9.arwo.core.alert.view.AlertsView;
import io.github.greyp9.arwo.core.app.App;
import io.github.greyp9.arwo.core.bundle.Bundle;
import io.github.greyp9.arwo.core.date.XsdDateU;
import io.github.greyp9.arwo.core.locus.Locus;
import io.github.greyp9.arwo.core.util.CollectionU;
import io.github.greyp9.arwo.core.xed.action.XedAction;
import io.github.greyp9.arwo.core.xed.model.Xed;
import io.github.greyp9.arwo.core.xed.model.XedFactory;
import io.github.greyp9.arwo.core.xed.nav.XedNav;
import io.github.greyp9.arwo.core.xed.view.XedPropertyPageView;
import io.github.greyp9.arwo.core.xed.view.html.PropertyStripHtmlView;
import io.github.greyp9.irby.core.cron.core.CronRequest;
import io.github.greyp9.irby.core.cron.service.CronService;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;

/**
 * Add alert indicating standby status of {@link CronService}, and providing UI to update status.
 */
public class CronStandbyView {

    public void addContentTo(final Element body, final Bundle bundle, final Locus locus,
                             final CronRequest cronRequest, final CronService cronService) throws IOException {
        final Date dateStandby = cronService.getDateStandby();
        if (dateStandby.after(new Date())) {
            final String message = String.format("Scheduling of jobs paused until %s", XsdDateU.toXSDZ(dateStandby));
            final Alerts alerts = AlertU.create(new Alert(Alert.Severity.INFO, message));
            new AlertsView(true, alerts, locus, bundle, null).addContentTo(body);
        }

        final XedAction actionStandby = new XedAction(
                new QName(App.Actions.URI_ACTION, Const.STANDBY, App.Actions.PREFIX_ACTION),
                new XedFactory(), locus.getLocale());
        final Xed xedUI = actionStandby.getXedUI(actionStandby.getXed().getLocale());
        final XedPropertyPageView pageView = new XedPropertyPageView(null, new XedNav(xedUI).getRoot());
        final Bundle bundleXed = xedUI.getBundle();
        final ActionFactory factory = new ActionFactory(
                cronRequest.getSubmitID(), bundleXed, App.Target.USER_STATE, Const.STANDBY, null);
        final Collection<String> actions = CollectionU.toCollection(App.Action.UPDATE);
        final ActionButtons buttons = factory.create(Const.STANDBY, false, actions);
        new PropertyStripHtmlView(pageView, buttons).addContentDiv(body);
    }

    private static class Const {
        private static final String STANDBY = "standby";
    }
}
