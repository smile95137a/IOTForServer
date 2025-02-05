package src.main.java.com.frontend.mapper;

import java.util.List;
import java.util.stream.Collectors;

import src.main.java.com.frontend.entity.channel.Channel;
import src.main.java.com.frontend.req.channel.ChannelReq;
import src.main.java.com.frontend.res.channel.ChannelRes;
import org.springframework.stereotype.Component;


@Component
public class ChannelMapper {

    public ChannelRes mapToChannelRes(Channel channel) {
        return ChannelRes.builder()
                .uid(channel.getUid())
                .name(channel.getName())
                .lineToken(channel.getLineToken())
                .lineSecret(channel.getLineSecret())
                .build();
    }

    public List<ChannelRes> mapToChannelResList(List<Channel> channels) {
        return channels.stream()
                .map(this::mapToChannelRes)
                .collect(Collectors.toList());
    }

    public Channel mapToChannel(ChannelReq channelReq) {
        return Channel.builder()
        		.uid(com.mo.app.utils.RandomUtils.genRandom(64))
                .name(channelReq.getName())
                .lineToken(channelReq.getLineToken())
                .lineSecret(channelReq.getLineSecret())
                .build();
    }

    public List<Channel> mapToChannelList(List<ChannelReq> channelReqs) {
        return channelReqs.stream()
                .map(this::mapToChannel)
                .collect(Collectors.toList());
    }
}
