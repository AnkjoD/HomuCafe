package com.homucafe.shared.ui;

import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;

/** Minh họa đồ uống bằng Java2D. Không tải ảnh mạng và không chứa xử lý nghiệp vụ. */
public final class DrinkIllustration extends JPanel {
    private final int kind;
    public DrinkIllustration(int kind) {
        this.kind=kind;setOpaque(false);setPreferredSize(new Dimension(160,130));
    }
    @Override protected void paintComponent(Graphics graphics){
        Graphics2D g=(Graphics2D)graphics.create();
        try{
            Theme.smooth(g);
            Color[] backgrounds={new Color(238,225,207),new Color(232,224,211),new Color(245,225,205),new Color(226,235,212),new Color(234,219,207),new Color(237,232,218)};
            g.setColor(backgrounds[kind%6]);g.fillRoundRect(0,0,getWidth(),getHeight(),16,16);
            double scale=Math.min(getWidth()/180.0,getHeight()/150.0);g.translate(getWidth()/2.0,getHeight()/2.0+3);g.scale(scale,scale);
            g.setColor(new Color(70,50,25,20));g.fill(new Ellipse2D.Double(-46,43,92,15));
            if(kind==2||kind==3||kind==5){
                Path2D glass=new Path2D.Double();glass.moveTo(-32,-48);glass.lineTo(32,-48);glass.lineTo(25,47);glass.quadTo(0,55,-25,47);glass.closePath();
                g.setColor(new Color(255,255,255,180));g.fill(glass);
                Shape clip=g.getClip();g.clip(glass);
                g.setColor(kind==2?new Color(217,151,77):kind==3?new Color(153,175,110):new Color(226,207,176));g.fillRect(-35,-30,70,82);
                g.setColor(kind==3?new Color(235,236,207):new Color(246,229,193));g.fillRect(-35,12,70,38);
                g.setColor(new Color(255,255,255,95));for(int i=0;i<4;i++)g.fillRoundRect(-23+(i%2)*24,-24+(i/2)*24,19,17,5,5);
                g.setClip(clip);g.setColor(new Color(255,255,255,200));g.setStroke(new BasicStroke(2));g.draw(glass);
                g.setColor(new Color(98,92,66));g.setStroke(new BasicStroke(4,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));g.drawLine(15,-12,27,-65);
                if(kind==2){g.setColor(new Color(228,135,69));g.fill(new Ellipse2D.Double(-45,20,27,27));g.setColor(new Color(112,137,75));g.fill(new Ellipse2D.Double(-32,15,18,7));}
            }else{
                g.setStroke(new BasicStroke(10));g.setColor(new Color(251,249,240));g.draw(new Ellipse2D.Double(24,-13,29,35));
                g.setColor(new Color(255,253,245));g.fillRoundRect(-37,-34,74,77,18,18);
                g.setColor(new Color(232,225,211));g.fill(new Ellipse2D.Double(-37,-45,74,25));
                g.setColor(kind==4?new Color(99,60,41):new Color(134,89,57));g.fill(new Ellipse2D.Double(-31,-40,62,17));
                g.setColor(new Color(240,218,181));g.setStroke(new BasicStroke(3));
                if(kind==1){g.draw(new Ellipse2D.Double(-17,-36,34,9));g.draw(new Ellipse2D.Double(-9,-34,18,5));}
                else{g.drawArc(-13,-36,24,9,10,170);}
                g.setColor(new Color(255,255,255,150));g.setStroke(new BasicStroke(2,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));g.draw(new QuadCurve2D.Double(-14,-53,-25,-64,-15,-73));g.draw(new QuadCurve2D.Double(4,-53,-7,-64,4,-74));
            }
        }finally{g.dispose();}
    }
}
