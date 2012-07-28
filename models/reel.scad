difference() {
intersection() {
  translate([-50, -50, 0])
  linear_extrude(file="reel-top.dxf", height=16);

  rotate_extrude(file="reel-side.dxf");
}

// bolt holes
translate([0, 15, 3.75]) rotate([0, 90, 0]) cylinder(h=20, r=2, center=true);
translate([0, -15, 3.75]) rotate([0, 90, 0]) cylinder(h=20, r=2, center=true);
}
