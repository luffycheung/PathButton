
package de.cbecker.pathbutton;

import android.app.Activity;
import android.os.Bundle;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class PathButtonActivity extends Activity implements PathMenu.OnSubMenuListener {

    private RelativeLayout layout;

    private TextView indicator;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        layout = (RelativeLayout)findViewById(R.id.path_layout);
        indicator = (TextView)findViewById(R.id.path_indicator);

        buildCustomLayout(PathMenu.Position.TopLeft, 3);
        buildCustomLayout(PathMenu.Position.TopRight, 4);
        buildCustomLayout(PathMenu.Position.BottomRight, 5);
        buildCustomLayout(PathMenu.Position.BottomLeft, 6);
    }

    private void buildCustomLayout(PathMenu.Position position, int subMenuSize) {
        ArrayList<PathMenuEntry> entries = new ArrayList<PathMenuEntry>();
        for (int i = 0; i < subMenuSize; ++i) {
            entries.add(new PathMenuEntry("" + i));
        }

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT);
        layout.addView(new PathMenu(this, position, entries, this), lp);
    }

    @Override
    public void onSubMenuEvent(PathMenuEntry entry) {
        indicator.setText("SubMenuButtonClick: " + entry.getName());
    }

}
