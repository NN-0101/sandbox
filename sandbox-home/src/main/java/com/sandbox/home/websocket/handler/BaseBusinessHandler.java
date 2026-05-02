package com.sandbox.home.websocket.handler;

import com.sandbox.home.websocket.model.BaseUpMsgBO;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * 业务处理器基类 - 责任链模式
 * <p>
 * 根据消息类型（messageType）将消息路由到对应的子类处理器，
 * 类型不匹配则传递给下一个处理器。子类只需实现 {@link #getHandlerType()} 和 {@link #process}。
 *
 * <h3>设计要点</h3>
 * <ul>
 *   <li><b>类型路由：</b>messageType 匹配 → process()，不匹配 → 传递下一个</li>
 *   <li><b>模板方法：</b>子类只写业务逻辑，路由由基类处理</li>
 *   <li><b>泛型支持：</b>T 指定消息类型，如 BaseBusinessHandler&lt;UpMsgBO&gt;</li>
 * </ul>
 *
 * @param <T> 消息类型，必须继承 BaseUpMsgBO
 * @author 0101
 * @since 2026-03-16
 */
public abstract class BaseBusinessHandler<T extends BaseUpMsgBO> extends SimpleChannelInboundHandler<T> {

    /**
     * 业务处理（模板方法，子类实现）
     */
    protected abstract void process(ChannelHandlerContext ctx, T msg);

    /**
     * 返回当前处理器支持的消息类型值（对应 UpMsgTypeEnum）
     */
    public abstract int getHandlerType();

    /**
     * 消息路由：类型匹配 → 处理，不匹配 → 传递下一个
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, T msg) {
        if (msg.getMessageType() == getHandlerType()) {
            process(ctx, msg);
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    /**
     * 异常默认传递给下一个处理器
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.fireExceptionCaught(cause);
    }
}