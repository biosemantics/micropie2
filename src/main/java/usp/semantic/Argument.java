package usp.semantic;

import usp.syntax.Path;
import usp.syntax.TreeNode;

public class Argument {
	TreeNode argNode_;
	Path path_;
	Part argPart_;
	public Argument(TreeNode argNode, Path path, Part argPart) {
		argNode_=argNode;
		path_=path;
		argPart_=argPart;
	}
}
