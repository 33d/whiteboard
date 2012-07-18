intersection() {
  translate([-50, -50, 0])
  linear_extrude(file="reel-top.dxf", height=16);

  rotate_extrude(file="reel-side.dxf");
}

