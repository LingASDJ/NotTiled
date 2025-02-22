package com.mirwanda.nottiled.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextArea;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.mirwanda.nottiled.NotiledAndoridPro;
import com.mirwanda.nottiled.layerhistory;

import de.tomgrill.gdxdialogs.core.listener.TextPromptListener;

public class NotTiledServer {
    /**
     * 多人协作核心载入函数
     * @param game NotTiledAndoridPro游戏实例
     */
    public static void loadKryonet(NotiledAndoridPro game){
        game.server = new Server(9999999,9999999);
        game.serverkryo = game.server.getKryo();
        game.serverkryo.register(NotiledAndoridPro.TextChat.class);
        game.serverkryo.register(layerhistory.class);
        game.serverkryo.register(NotiledAndoridPro.PlayerState.class);
        game.serverkryo.register(NotiledAndoridPro.command.class);

        game.client = new Client();
        game.clientkryo = game.client.getKryo();
        game.clientkryo.register(NotiledAndoridPro.TextChat.class);
        game.clientkryo.register(layerhistory.class);
        game.clientkryo.register(NotiledAndoridPro.PlayerState.class);
        game.clientkryo.register(NotiledAndoridPro.command.class);

        game.server.addListener(new Listener() {
            public void received (Connection connection, Object object) {
                if (object instanceof NotiledAndoridPro.TextChat) {
                    NotiledAndoridPro.TextChat request = (NotiledAndoridPro.TextChat)object;
                    game.broadcast(request.text);
                    game.lcollabstatus.setText(request.text);

                } else if (object instanceof NotiledAndoridPro.command) {
                    NotiledAndoridPro.command cmd = (NotiledAndoridPro.command) object;
                    switch (cmd.command){
                        case "registerID":
                            NotiledAndoridPro.actvClients acv = new NotiledAndoridPro.actvClients();
                            acv.id = cmd.data;
                            acv.room="";
                            game.activeClients.add(acv);
                            game.logNet("[S] Registration requested by: " + acv.id);
                            game.activeClients.add( acv );
                            NotiledAndoridPro.command cc = new NotiledAndoridPro.command();
                            cc.from=acv.id;
                            cc.command = "registered";
                            connection.sendTCP(cc);

                            break;
                        case "createRoom":
                            boolean roomok=true;
                            for (NotiledAndoridPro.actvClients av : game.activeClients){
                                if (av.creator && av.room.equalsIgnoreCase(cmd.room)) {
                                    roomok = false;
                                    break;
                                }
                            }

                            if (roomok){


                                for (NotiledAndoridPro.actvClients av : game.activeClients){
                                    if (av.id.equalsIgnoreCase( cmd.from )){
                                        av.room = cmd.room;
                                        av.creator = true;
                                    }
                                }
                                cc = new NotiledAndoridPro.command();
                                cc.command = "roomCreateOK";
                                connection.sendTCP(cc);
                                game.logNet("[S] Created room: " + cmd.room);
                            }
                            else
                            {
                                cc = new NotiledAndoridPro.command();
                                cc.command = "roomCreateFailed";
                                connection.sendTCP(cc);
                                game.logNet("[S] Room not created: " + cmd.room);
                            }
                            break;
                        case "destroyRoom":
                            for (NotiledAndoridPro.actvClients av : game.activeClients){
                                if (av.room.equalsIgnoreCase( cmd.room )){
                                    av.room = "";
                                    av.creator = false;
                                }
                            }
                            cc = new NotiledAndoridPro.command();
                            cc.command = "roomDestroyed";
                            cc.room = cmd.room;
                            game.server.sendToAllTCP( cc );
                            game.logNet("[S] Broadcasted room destruction: " + cmd.room);
                            break;


                        case "joinRequest":
                            //check if the room is available
                            boolean isavail = false;
                            for (NotiledAndoridPro.actvClients av : game.activeClients){
                                if (av.room.equalsIgnoreCase(cmd.room) && av.creator) {
                                    isavail = true;
                                    break;
                                }
                            }

                            if (isavail){
                                //room is available
                                for (NotiledAndoridPro.actvClients av : game.activeClients){
                                    if (av.id.equalsIgnoreCase( cmd.from )){
                                        av.room = cmd.room;
                                    }
                                }

                                cc = new NotiledAndoridPro.command();
                                cc.command = "joinRequestAccepted";
                                cc.room = cmd.room;
                                connection.sendTCP(cc);
                                game.logNet("[S] Join Request accepted for: " + cmd.from + " @ room:" + cmd.room);

                                game.logNet("[SB] broadcasting join information");
                                cc = new NotiledAndoridPro.command();
                                cc.command = "joinInformation";
                                cc.room = cmd.room;
                                cc.data = cmd.from;
                                game.server.sendToAllTCP( cc );
                            } else{
                                //room is not available
                                cc = new NotiledAndoridPro.command();
                                cc.command = "joinRequestRejected";
                                cc.room = cmd.room;
                                connection.sendTCP(cc);
                                game.logNet("[S] Join Request rejected for: " + cmd.from + " @ room:" + cmd.room);
                            }



                            break;
                        case "leaveRequest":
                            for (NotiledAndoridPro.actvClients av : game.activeClients){
                                if (av.id.equalsIgnoreCase( cmd.from )){
                                    av.room = "";
                                }
                            }
                            game.logNet("[S] left room for: " +cmd.from +" @ "+ cmd.room);
                            cc = new NotiledAndoridPro.command();
                            cc.command = "leaveRequestAccepted";
                            cc.room = cmd.room;
                            connection.sendTCP( cc );

                            game.logNet("[SB] broadcasting leave information");
                            cc = new NotiledAndoridPro.command();
                            cc.command = "leaveInformation";
                            cc.room = cmd.room;
                            cc.data = cmd.from;
                            game.server.sendToAllTCP( cc );

                            break;
                        case "startdata":
                        case "data":
                        case "startDataAll":
                        case "dataAll":
                        case "draw":
                            game.server.sendToAllTCP( cmd );
                            break;
                        case "readThisBoy":
                            cc = new NotiledAndoridPro.command();
                            cc.command = "mapInformation";
                            cc.from = cmd.from;
                            cc.room = cmd.room;
                            game.server.sendToAllTCP( cc );
                            game.logNet("[SB] broadcasting map information");


                            break;
                        case "allReadThis":
                            cc = new NotiledAndoridPro.command();
                            cc.command = "mapInformationAll";
                            cc.from = cmd.from;
                            cc.room = cmd.room;
                            game.server.sendToAllTCP( cc );
                            game.logNet("[SB] broadcasting open map information");


                            break;
                        //}catch(Exception e){
                        //    ErrorBung( e,"MOMON.TXT" );
                        //}
                        case "disconnect":
                            game.logNet("[S] Disconnect Request...");
                            int flag=-1;
                            for (int i=0;i<game.activeClients.size();i++){
                                if (game.activeClients.get( i ).id.equalsIgnoreCase( cmd.from )){
                                    flag=i;
                                }

                            }
                            if (flag!=-1){
                                game.activeClients.remove( flag );
                                game.logNet("[S] Client erased.");
                            }

                    }
                } else if (object instanceof layerhistory) {
                    layerhistory response = (layerhistory)object;
                    Gdx.app.log("hi","received from client");
                    game.pushdata(response);
                }
            }
        });

        game.client.addListener(new Listener() {
            public void received (Connection connection, Object object) {

                if (object instanceof NotiledAndoridPro.command) {
                    NotiledAndoridPro.command cmd = (NotiledAndoridPro.command) object;
                    switch (cmd.command) {
                        case "registered":
                            game.logNet( game.z.regroom + cmd.from);
                            break;
                        case "roomCreateFailed":
                            game.logNet( game.z.regalredyexits);
                            break;
                        case "roomCreateOK":
                            game.isCreateRoom = true;
                            game.activeRoom = game.roomName.getText();
                            game.tbCreateRoom.setText(game.z.destyroom);
                            game.logNet(game.z.createseroom + game.roomName.getText());
                            break;
                        case "roomDestroyed":
                            if (cmd.room.equalsIgnoreCase( game.activeRoom )){
                                game.isCreateRoom = false;
                                game.isJoinRoom = false;
                                game.activeRoom = "";
                                game.tbCreateRoom.setText(game.z.createroom);
                                game.tbJoinRoom.setText(game.z.joinroom);
                                game.logNet(game.z.destyroomalt + game.roomName.getText());
                            }
                            break;
                        case "joinRequestAccepted":
                            game.logNet( game.z.joinroomalt +cmd.room);
                            game.logNet( game.z.loadingmap);
                            game.isJoinRoom = true;
                            game.activeRoom = cmd.room;
                            game.tbJoinRoom.setText( game.z.leaveroom );
                            break;
                        case "joinRequestRejected":
                            game.logNet( game.z.failedroom +cmd.room);
                            break;
                        case "leaveRequestAccepted":
                            game.logNet( game.z.leftroom +cmd.room);
                            game.isJoinRoom = false;
                            game.activeRoom = "";
                            game.tbJoinRoom.setText( game.z.joinroom);

                            break;
                        case "leaveInformation":
                            if (cmd.room.equalsIgnoreCase(game.activeRoom)) {
                                game.logNet( game.z.console +cmd.data+game.z.playerleaveroom);
                            }
                            break;
                        case "startData":
                            if (cmd.from.equalsIgnoreCase( game.myID )) {
                                game.clientMapData = "";
                            }
                            break;
                        case "data":
                            if (cmd.from.equalsIgnoreCase( game.myID )) {
                                game.clientMapData += cmd.data;
                            }
                            break;
                        case "startDataAll":
                            if (!cmd.from.equalsIgnoreCase( game.myID ) && cmd.room.equalsIgnoreCase(game.activeRoom)) {
                                game.clientMapData = "";
                            }
                            break;
                        case "dataAll":
                            if (!cmd.from.equalsIgnoreCase( game.myID ) && cmd.room.equalsIgnoreCase(game.activeRoom)) {
                                game.clientMapData += cmd.data;
                            }
                            break;
                        case "joinInformation":
                            if (cmd.room.equalsIgnoreCase(game.activeRoom) && !cmd.data.equalsIgnoreCase( game.myID )) {
                                game.logNet( game.z.console +cmd.data+game.z.playerjoinroom);
                                if (game.isCreateRoom){
                                    //send the map
                                    game.saveMap(game.curdir + "/" + game.curfile);
                                    //read map as text
                                    FileHandle ff = Gdx.files.absolute( game.curdir +"/"+ game.curfile );
                                    game.logNet( game.z.sendmapdata);

                                    String dat = ff.readString();
                                    ///////
                                    NotiledAndoridPro.command cc = new NotiledAndoridPro.command();
                                    cc.command = "startData";
                                    cc.from = cmd.data;
                                    cc.room = cmd.room;
                                    connection.sendTCP( cc );

                                    int len = dat.length();
                                    for (int i=0;i<len;i+=game.BufferSize){
                                        String part = dat.substring(i, Math.min(len, i + game.BufferSize));
                                        cc = new NotiledAndoridPro.command();
                                        cc.command = "data";
                                        cc.room = cmd.room;
                                        cc.from = cmd.data;
                                        cc.data = part;
                                        game.slowdown();
                                        connection.sendTCP( cc );
                                    }
                                    cc = new NotiledAndoridPro.command();
                                    cc.command = "readThisBoy";
                                    cc.from = cmd.data;
                                    connection.sendTCP( cc );
                                }
                            }
                            break;
                        case "mapInformation":
                            if (cmd.from.equalsIgnoreCase( game.myID )) {
                                game.logNet( game.z.sendmapar);
                                FileHandle fh = Gdx.files.absolute( game.basepath+"NotTiled/tempNetworkMap.tmx" );
                                fh.writeString( game.clientMapData,false);
                                game.clientMapData="";
                                game.backToMap();
                                game.loadtmx( game.basepath+"NotTiled/tempNetworkMap.tmx"  );
                            }
                            break;
                        case "mapInformationAll":
                            if (!cmd.from.equalsIgnoreCase( game.myID ) && cmd.room.equalsIgnoreCase(game.activeRoom)) {
                                game.logNet( game.z.sendmapar);
                                FileHandle fh = Gdx.files.absolute( game.curdir + "/" + game.curfile);

                                fh.writeString( game.clientMapData,false);
                                game.clientMapData="";
                                game.backToMap();
                                game.loadtmx( game.curdir + "/" + game.curfile );
                            }
                            break;
                        case "draw":
                            if (cmd.room.equalsIgnoreCase(game.activeRoom) && !cmd.from.equalsIgnoreCase( game.myID )) {
                                layerhistory h = cmd.lh;
                                if (h.undo) {

                                    long frm = h.from;
                                    long toe = h.to;
                                    int frmts = h.oldtset;
                                    int toets = h.newtset;
                                    int frmtl = h.oldtile;
                                    int toetl = h.newtile;
                                    h.from = toe;
                                    h.to = frm;
                                    h.oldtset = toets;
                                    h.newtset = frmts;
                                    h.undo = false;
                                    h.oldtile = toetl;
                                    h.newtile = frmtl;
                                }
                                game.undolayer.add(h);
                                game.redolayer.clear();
                                game.layers.get(h.getLayer()).getStr().set(h.getLocation(), h.getTo());
                                game.layers.get(h.getLayer()).getTset().set(h.getLocation(), h.getNewtset());
                                game.layers.get(h.getLayer()).getTile().set(h.getLocation(), h.getNewtile());
                                game.updateCache(h.getLocation());
                            }
                            break;
                    }
                }

            }

        });

        game.tCollab = new Table();
        game.tCollab.setFillParent(true);
        game.tCollab.defaults().width(game.btnx).height(game.btny).padBottom(2);

        game.tCollab1 = new Table();
        game.tCollab1.defaults().width(game.btnx).height(game.btny).padBottom(2);

        game.tCollab2 = new Table();
        game.tCollab2.defaults().width(game.btnx).height(game.btny).padBottom(2);

        Label lTitle = new Label(game.z.collaboration,game.skin);
        game.tbHost = new TextButton(game.z.runserver,game.skin);

        //知捷云
        game.tfRemoteIP = new TextField("127.0.0.1",game.skin);

        game.tfPort = new TextField("45372",game.skin);
        game.tbJoin = new TextButton(game.z.join,game.skin);
        game.roomName = new TextField("room1", game.skin);
        game.uniqueID = new TextField("Steve", game.skin);
        game.tbCreateRoom = new TextButton(game.z.createroom,game.skin);
        game.tbJoinRoom = new TextButton(game.z.joinroom,game.skin);
        game.clearLog = new TextButton(game.z.clearlog,game.skin);
        game.pushUpdateBtn = new TextButton(game.z.pushupdate,game.skin);
        game.tbJoin = new TextButton(game.z.join,game.skin);
        game.netLog = new TextArea("",game.skin);
        TextButton tbBack = new TextButton(game.z.back,game.skin);
        game.lcollabstatus = new Label(game.z.status+": "+game.z.readytoconnect,game.skin);
        final TextField tfMessage = new TextField("",game.skin);
        TextButton tbSendMsg = new TextButton(game.z.sendmessage,game.skin);

        game.pushUpdateBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.pushUpdate();
            }
        });

        tbSendMsg.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {

                if (game.isServer){
                    game.broadcast( tfMessage.getText() );
                    tfMessage.setText( "" );
                }
                if (game.isClient){
                    game.talktoserver(tfMessage.getText());
                    tfMessage.setText("");
                }
            }
        });


        tbBack.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.backToMap();
            }
        });

        game.til = new TextPromptListener() {

            @Override
            public void confirm(String input) {
                if (input.isEmpty()) {
                    return;
                }

                game.runServer(Integer.parseInt(input));
            }

            @Override
            public void cancel() {
            }

        };


        game.tbHost.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (!game.isServer) {
                    game.runServer( Integer.parseInt( game.tfPort.getText() ) );
                }else
                {
                    game.stopServer();
                }
            }
        });

        game.tbJoin.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {

                if (!game.isClient) {
                    game.runClient(game.tfRemoteIP.getText(), Integer.parseInt(game.tfPort.getText()));
                }else
                {
                    game.stopClient();
                }
            }
        });

        game.tbCreateRoom.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {

                if (!game.isCreateRoom) {
                    game.createRoom();
                }else
                {
                    game.destroyRoom();
                }
            }
        });

        game.tbJoinRoom.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {

                if (!game.isJoinRoom) {
                    game.joinRoom();
                }else
                {
                    game.leaveRoom();
                }
            }
        });

        game.clearLog.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.netLog.setText("");
            }
        });

        game.tCollab1.add(lTitle).colspan(2).row();
        game.tCollab1.add(new Label(game.z.uniqueid,game.skin)).width(game.btnx/2f);
        game.tCollab1.add(game.uniqueID).width(game.btnx/2f).row();
        game.tCollab1.add(game.tbJoin).colspan(2).row();

        game.tCollab1.add(new Label(game.z.room,game.skin)).width(game.btnx/2f);
        game.tCollab1.add(game.roomName).width(game.btnx/2f).row();
        game.tCollab1.add(game.tbCreateRoom).colspan(2).row();
        game.tCollab1.add(game.tbJoinRoom).colspan(2).row();
        game.tCollab1.add(game.pushUpdateBtn).colspan(2).row();
        game.tCollab1.add(tbBack).colspan(2).row();
        game.tCollab1.add(game.lcollabstatus).colspan(2).row();

        game.tCollab2.add(new Label(game.z.netlog,game.skin)).colspan(2).row();
        game.tCollab2.add(game.netLog).height(game.btny*5).colspan(2).row();
        game.tCollab.add( game.tCollab1 );
    }
}
