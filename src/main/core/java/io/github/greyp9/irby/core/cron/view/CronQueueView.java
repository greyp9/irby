package io.github.greyp9.irby.core.cron.view;

import io.github.greyp9.arwo.core.app.App;
import io.github.greyp9.arwo.core.bundle.Bundle;
import io.github.greyp9.arwo.core.locus.Locus;
import io.github.greyp9.arwo.core.table.html.TableView;
import io.github.greyp9.arwo.core.table.model.Table;
import io.github.greyp9.arwo.core.table.model.TableContext;
import io.github.greyp9.arwo.core.table.state.ViewState;
import io.github.greyp9.irby.core.cron.core.CronRequest;
import io.github.greyp9.irby.core.cron.service.CronService;
import org.w3c.dom.Element;

import java.io.IOException;

/**
 * Render table with pending jobs in specified {@link CronService}.
 */
public class CronQueueView {

    public final void addContentTo(final Element body, final Bundle bundle, final Locus locus,
                             final CronRequest cronRequest, final CronService cronService) throws IOException {
        final Table table = cronService.getQueue();
        final TableContext tableContext = new TableContext(
                new ViewState(null), null, cronRequest.getSubmitID(), App.CSS.TABLE, bundle, locus);
        final TableView tableView = new TableView(table, tableContext);
        tableView.addContentTo(body);
    }
}
