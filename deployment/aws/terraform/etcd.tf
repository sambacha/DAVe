#########################
# etcd cluster instances
#########################

resource "aws_instance" "etcd" {
    count = 3
    ami = "${var.default_ami}"
    instance_type = "${var.etcd_instance_type}"

    subnet_id = "${aws_subnet.kubernetes.id}"
    private_ip = "${cidrhost(var.vpc_cidr, 10 + count.index)}"
    associate_public_ip_address = true # Instances have public, dynamic IP

    availability_zone = "${var.zone}"
    vpc_security_group_ids = ["${aws_security_group.kubernetes.id}"]
    key_name = "${var.default_keypair_name}"

    tags {
      Name = "etcd-${count.index}"
      Owner = "${var.owner}"
      Application = "${var.application}"
      Confidentiality = "${var.confidentality}"
      Costcenter = "${var.costcenter}"
      ansibleFilter = "${var.ansibleFilter}"
      ansibleNodeType = "etcd"
      ansibleNodeName = "etcd${count.index}"
    }
}
