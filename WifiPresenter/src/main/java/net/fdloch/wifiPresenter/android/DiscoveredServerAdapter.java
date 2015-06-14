package net.fdloch.wifiPresenter.android;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import net.fdloch.wifiPresenter.android.network.ServiceDiscovery;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by florian on 14.06.15.
 */
public class DiscoveredServerAdapter extends BaseAdapter {
    public static final long ID_CUSTOM_CONNECTION = -1;

    private List<ServiceDiscovery.ServerInformation> discoveredServers = new ArrayList<ServiceDiscovery.ServerInformation>();

    public void add(ServiceDiscovery.ServerInformation server) {
        this.discoveredServers.add(server);
        notifyDataSetChanged();
    }

    public boolean isCustomConnectionItem(long id) {
        return id == ID_CUSTOM_CONNECTION;
    }

    @Override
    public int getCount() {
        return this.discoveredServers.size() + 1;
    }

    @Override
    public Object getItem(int position) {
        if (position < this.discoveredServers.size()) {
            return this.discoveredServers.get(position);
        }

        return null;
    }

    @Override
    public long getItemId(int position) {
        if (position == this.discoveredServers.size()) {
            return ID_CUSTOM_CONNECTION;
        }

        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

        if (null == view) {
            view = this.createNewListItemView(parent);
        }

        String hostname = "Click here to enter a custom server!";
        String ip = "";

        if (position < this.discoveredServers.size()) {
            ServiceDiscovery.ServerInformation serverInfo = this.discoveredServers.get(position);
            hostname = serverInfo.getHostname();
            ip = serverInfo.getAddress().getHostAddress();
        }

        ViewHolder holder = (ViewHolder) view.getTag();
        holder.gettV_hostname().setText(hostname);
        holder.gettV_ip().setText(ip);

        return view;
    }

    private static View createNewListItemView(ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.server_selection_listitem, parent, false);

        TextView tv_hostname = (TextView) view.findViewById(R.id.tv_hostname);
        TextView tv_ip = (TextView) view.findViewById(R.id.tv_ip);

        view.setTag(new ViewHolder(tv_ip, tv_hostname));

        return view;
    }

    public static class ViewHolder {
        TextView tV_ip;
        TextView tV_hostname;

        public ViewHolder(TextView tV_ip, TextView tV_hostname) {
            this.tV_ip = tV_ip;
            this.tV_hostname = tV_hostname;
        }

        public TextView gettV_ip() {
            return tV_ip;
        }

        public TextView gettV_hostname() {
            return tV_hostname;
        }
    }
}
