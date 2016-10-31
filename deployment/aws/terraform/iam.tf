##########################
# IAM: Policies and Roles
##########################

# The following Roles and Policy are mostly for future use

/*resource "aws_iam_role" "kubernetes" {
  name = "dave-k8s"
  assume_role_policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "Service": "ec2.amazonaws.com"
      },
      "Action": "sts:AssumeRole"
    }
  ]
}
EOF
}

# Role policy
resource "aws_iam_role_policy" "kubernetes" {
  name = "dave-k8s"
  role = "${aws_iam_role.kubernetes.id}"
  policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Action" : ["ec2:*"],
      "Effect": "Allow",
      "Resource": ["*"]
    },
    {
      "Action" : ["elasticloadbalancing:*"],
      "Effect": "Allow",
      "Resource": ["*"]
    },
    #{
    #  "Action": "route53:*",
    #  "Effect": "Allow",
    #  "Resource": ["*"]
    #},
    {
      "Action": "ecr:*",
      "Effect": "Allow",
      "Resource": "*"
    }
  ]
}
EOF
}

# IAM Instance Profile for Controller
resource  "aws_iam_instance_profile" "kubernetes" {
 name = "dave-k8s"
 roles = ["${aws_iam_role.kubernetes.name}"]
}*/
