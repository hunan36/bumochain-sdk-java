package cn.bumo.access.utils.console;

/**
 * 命令处理器；
 *
 * @author 布萌
 */
public interface CommondProcessor{

    public void onEnter(String command, String[] args, CommandConsole console);

}
